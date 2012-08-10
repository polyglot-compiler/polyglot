/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An <code>ArrayInit</code> is an immutable representation of
 * an array initializer, such as { 3, 1, { 4, 1, 5 } }.  Note that
 * the elements of these array may be expressions of any type (e.g.,
 * <code>Call</code>).
 */
public class ArrayInit_c extends Expr_c implements ArrayInit {
    protected List<Expr> elements;

    public ArrayInit_c(Position pos, List<Expr> elements) {
        super(pos);
        assert (elements != null);
        this.elements = ListUtil.copy(elements, true);
    }

    /** Get the elements of the initializer. */
    @Override
    public List<Expr> elements() {
        return this.elements;
    }

    /** Set the elements of the initializer. */
    @Override
    public ArrayInit elements(List<Expr> elements) {
        ArrayInit_c n = (ArrayInit_c) copy();
        n.elements = ListUtil.copy(elements, true);
        return n;
    }

    /** Reconstruct the initializer. */
    protected ArrayInit_c reconstruct(List<Expr> elements) {
        if (!CollectionUtil.equals(elements, this.elements)) {
            ArrayInit_c n = (ArrayInit_c) copy();
            n.elements = ListUtil.copy(elements, true);
            return n;
        }

        return this;
    }

    /** Visit the children of the initializer. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Expr> elements = visitList(this.elements, v);
        return reconstruct(elements);
    }

    /** Type check the initializer. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        Type type = null;

        for (Expr e : elements) {
            if (type == null) {
                type = e.type();
            }
            else {
                type = ts.leastCommonAncestor(type, e.type());
            }
        }

        if (type == null) {
            return type(ts.Null());
        }
        else {
            return type(arrayOf(ts, type));
        }
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

        if (!t.isArray()) {
            throw new InternalCompilerError("Type of array initializer must "
                    + "be an array.", position());
        }

        t = t.toArray().base();

        TypeSystem ts = av.typeSystem();

        for (Expr e : elements) {
            if (e == child) {
                if (ts.numericConversionValid(t, e.constantValue())) {
                    return child.type();
                }
                else {
                    return t;
                }
            }
        }

        return child.type();
    }

    @Override
    public void typeCheckElements(Type lhsType) throws SemanticException {
        TypeSystem ts = lhsType.typeSystem();

        if (!lhsType.isArray()) {
            throw new SemanticException("Cannot initialize " + lhsType
                    + " with " + type + ".", position());
        }

        // Check if we can assign each individual element.
        Type t = lhsType.toArray().base();

        for (Expr e : elements) {
            Type s = e.type();

            if (e instanceof ArrayInit) {
                ((ArrayInit) e).typeCheckElements(t);
                continue;
            }

            if (!ts.isImplicitCastValid(s, t) && !ts.typeEquals(s, t)
                    && !ts.numericConversionValid(t, e.constantValue())) {
                throw new SemanticException("Cannot assign " + s + " to " + t
                        + ".", e.position());
            }
        }
    }

    @Override
    public String toString() {
        return "{ ... }";
    }

    /** Write the initializer to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("{ ");

        for (Iterator<Expr> i = elements.iterator(); i.hasNext();) {
            Expr e = i.next();
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
        return listChild(elements, (Expr) null);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(elements, this, EXIT);
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ArrayInit(this.position, this.elements);
    }
}
