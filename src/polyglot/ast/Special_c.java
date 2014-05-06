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

package polyglot.ast;

import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code Special} is an immutable representation of a
 * reference to {@code this} or {@code super} in Java.  This
 * reference can be optionally qualified with a type such as 
 * {@code Foo.this}.
 */
public class Special_c extends Expr_c implements Special {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Special.Kind kind;
    protected TypeNode qualifier;

//    @Deprecated
    public Special_c(Position pos, Special.Kind kind, TypeNode qualifier) {
        this(pos, kind, qualifier, null);
    }

    public Special_c(Position pos, Special.Kind kind, TypeNode qualifier,
            Ext ext) {
        super(pos, ext);
        assert (kind != null); // qualifier may be null
        this.kind = kind;
        this.qualifier = qualifier;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    @Override
    public Special.Kind kind() {
        return this.kind;
    }

    @Override
    public Special kind(Special.Kind kind) {
        return kind(this, kind);
    }

    protected <N extends Special_c> N kind(N n, Special.Kind kind) {
        if (n.kind == kind) return n;
        n = copyIfNeeded(n);
        n.kind = kind;
        return n;
    }

    @Override
    public TypeNode qualifier() {
        return this.qualifier;
    }

    @Override
    public Special qualifier(TypeNode qualifier) {
        return qualifier(this, qualifier);
    }

    protected <N extends Special_c> N qualifier(N n, TypeNode qualifier) {
        if (n.qualifier == qualifier) return n;
        n = copyIfNeeded(n);
        n.qualifier = qualifier;
        return n;
    }

    /** Reconstruct the expression. */
    protected <N extends Special_c> N reconstruct(N n, TypeNode qualifier) {
        n = qualifier(n, qualifier);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode qualifier = visitChild(this.qualifier, v);
        return reconstruct(this, qualifier);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();
        Context c = tc.context();

        ClassType t = null;

        if (qualifier == null) {
            // an unqualified "this" or "super"
            t = c.currentClass();
        }
        else {
            if (!qualifier.isDisambiguated()) {
                return this;
            }

            if (qualifier.type().isClass()) {
                t = qualifier.type().toClass();

                if (!c.currentClass().hasEnclosingInstance(t)) {
                    throw new SemanticException("The nested class \""
                                                        + c.currentClass()
                                                        + "\" does not have "
                                                        + "an enclosing instance of type \""
                                                        + t + "\".",
                                                qualifier.position());
                }
            }
            else {
                throw new SemanticException("Invalid qualifier for \"this\" or \"super\".",
                                            qualifier.position());
            }
        }

        if (t == null
                || (c.inStaticContext() && ts.equals(t, c.currentClass()))) {
            // trying to access "this" or "super" from a static context.
            throw new SemanticException("Cannot access a non-static "
                    + "field or method, or refer to \"this\" or \"super\" "
                    + "from a static context.", this.position());
        }

        if (kind == THIS) {
            return type(t);
        }
        else if (kind == SUPER) {
            return type(t.superType());
        }

        return this;
    }

    @Override
    public Term firstChild() {
        if (qualifier != null) {
            return qualifier;
        }

        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (qualifier != null) {
            v.visitCFG(qualifier, this, EXIT);
        }

        return succs;
    }

    @Override
    public String toString() {
        if (qualifier != null) {
            // we have a qualifier for the special
            // The JLS requires that a qualified this contains just a simple class name, so try and extract it.
            // (JLS 2nd ed 15.8.4)
            if (qualifier.name() != null) {
                return qualifier.name() + "." + kind;
            }
            else {
                return qualifier + "." + kind;
            }
        }

        return String.valueOf(kind);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (qualifier != null) {
            // we have a qualifier for the special
            // The JLS requires that a qualified this contains just a simple class name, so try and extract it.
            // (JLS 2nd ed 15.8.4)
            if (qualifier.name() != null) {
                w.write(qualifier.name());
            }
            else {
                tr.lang().prettyPrint(qualifier, w, tr);
            }
            w.write(".");
        }

        w.write(kind.toString());
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (kind != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(kind " + kind + ")");
            w.end();
        }
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Special(this.position, this.kind, this.qualifier);
    }
}
