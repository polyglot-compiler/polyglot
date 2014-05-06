/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ext.jl5.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An {@code ArrayInit} is an immutable representation of
 * an array initializer, such as { 3, 1, { 4, 1, 5 } }.  Note that
 * the elements of these array may be expressions of any type (e.g.,
 * {@code Call}).
 */
public class ElementValueArrayInit_c extends Term_c implements
        ElementValueArrayInit {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<Term> elements;

    protected Type type;

    public ElementValueArrayInit_c(Position pos, List<Term> elements) {
        super(pos);
        assert (elements != null);
        this.elements = ListUtil.copy(elements, true);
    }

    @Override
    public List<Term> elements() {
        return this.elements;
    }

    @Override
    public Node elements(List<Term> elements) {
        return elements(this, elements);
    }

    protected <N extends ElementValueArrayInit_c> N elements(N n,
            List<Term> elements) {
        ElementValueArrayInit_c ext = n;
        if (CollectionUtil.equals(ext.elements, elements)) return n;
        if (n == this) {
            n = Copy.Util.copy(n);
            ext = n;
        }
        ext.elements = ListUtil.copy(elements, true);
        return n;
    }

    @Override
    public Type type() {
        return this.type;
    }

    @Override
    public Node type(Type type) {
        return type(this, type);
    }

    protected <N extends ElementValueArrayInit_c> N type(N n, Type type) {
        ElementValueArrayInit_c ext = n;
        if (ext.type == type) return n;
        if (n == this) {
            n = Copy.Util.copy(n);
            ext = n;
        }
        ext.type = type;
        return n;
    }

    /** Reconstruct the initializer. */
    protected <N extends ElementValueArrayInit_c> N reconstruct(N n,
            List<Term> elements) {
        n = elements(n, elements);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Term> elements = visitList(this.elements, v);
        return reconstruct(this, elements);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        Type type = null;

        for (Term e : elements) {
            if (type == null) {
                type = typeOf(e);
            }
            else {
                type = ts.leastCommonAncestor(type, typeOf(e));
            }
        }

        if (type == null) {
            return type(ts.Null());
        }
        else {
            return type(arrayOf(ts, type));
        }
    }

    private static Type typeOf(Term e) {
        if (e instanceof Expr) {
            return ((Expr) e).type();
        }
        if (e instanceof ElementValueArrayInit) {
            return ((ElementValueArrayInit) e).type();
        }
        return null;
    }

    protected Type arrayOf(TypeSystem ts, Type baseType) {
        return ts.arrayOf(baseType);
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (elements.isEmpty()) {
            return child.type();
        }

        Type t = av.toType();
        if (t == null) {
            t = this.type();
        }
        if (!t.isArray()) {
            throw new InternalCompilerError("Type of array initializer must "
                    + "be an array.", position());
        }

        t = t.toArray().base();

        for (Term e : elements) {
            if (e == child) {
                // the expected type of the array element is the base type
                // of the array.
                return t;
            }
        }

        return child.type();
    }

    @Override
    public void typeCheckElements(TypeChecker tc, Type lhsType)
            throws SemanticException {
        TypeSystem ts = lhsType.typeSystem();

        if (!lhsType.isArray()) {
            throw new SemanticException("Cannot initialize " + lhsType
                    + " with " + type() + ".", position());
        }

        // Check if we can assign each individual element.
        Type t = lhsType.toArray().base();

        for (Term e : elements) {
            Type s = typeOf(e);

            if (e instanceof ElementValueArrayInit) {
                ((ElementValueArrayInit) e).typeCheckElements(tc, t);
                continue;
            }

            if (!ts.isImplicitCastValid(s, t)
                    && !ts.typeEquals(s, t)
                    && !ts.numericConversionValid(t,
                                                  tc.lang()
                                                    .constantValue((Expr) e,
                                                                   tc.lang()))) {
                throw new SemanticException("Cannot assign " + s + " to " + t
                        + ".", e.position());
            }
        }
    }

    @Override
    public String toString() {
        return "{ ... }";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("{ ");

        for (Iterator<Term> i = elements.iterator(); i.hasNext();) {
            Term e = i.next();
            print(e, w, tr);

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }

        w.write(" }");
    }

    @Override
    public Term firstChild() {
        return Term_c.listChild(elements, (Expr) null);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(elements, this, Term.EXIT);
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return ((JL5NodeFactory) nf).ElementValueArrayInit(position(),
                                                           this.elements);
    }
}
