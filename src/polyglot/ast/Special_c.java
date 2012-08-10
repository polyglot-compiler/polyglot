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

import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>Special</code> is an immutable representation of a
 * reference to <code>this</code> or <code>super</code in Java.  This
 * reference can be optionally qualified with a type such as 
 * <code>Foo.this</code>.
 */
public class Special_c extends Expr_c implements Special {
    protected Special.Kind kind;
    protected TypeNode qualifier;

    public Special_c(Position pos, Special.Kind kind, TypeNode qualifier) {
        super(pos);
        assert (kind != null); // qualifier may be null
        this.kind = kind;
        this.qualifier = qualifier;
    }

    /** Get the precedence of the expression. */
    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    /** Get the kind of the special expression, either this or super. */
    @Override
    public Special.Kind kind() {
        return this.kind;
    }

    /** Set the kind of the special expression, either this or super. */
    @Override
    public Special kind(Special.Kind kind) {
        Special_c n = (Special_c) copy();
        n.kind = kind;
        return n;
    }

    /** Get the qualifier of the special expression. */
    @Override
    public TypeNode qualifier() {
        return this.qualifier;
    }

    /** Set the qualifier of the special expression. */
    @Override
    public Special qualifier(TypeNode qualifier) {
        Special_c n = (Special_c) copy();
        n.qualifier = qualifier;
        return n;
    }

    /** Reconstruct the expression. */
    protected Special_c reconstruct(TypeNode qualifier) {
        if (qualifier != this.qualifier) {
            Special_c n = (Special_c) copy();
            n.qualifier = qualifier;
            return n;
        }

        return this;
    }

    /** Visit the children of the expression. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode qualifier = (TypeNode) visitChild(this.qualifier, v);
        return reconstruct(qualifier);
    }

    /** Type check the expression. */
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
        return (qualifier != null ? qualifier.type().toClass().name() + "."
                : "") + kind;
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (qualifier != null) {
            w.write(qualifier.type().toClass().name());
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
