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

import java.util.Collections;
import java.util.List;

import polyglot.main.Options;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

/**
 * An {@code Assert} is an immutable representation of an {@code assert}
 * statement.
 */
public class Assert_c extends Stmt_c implements Assert {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr cond;
    protected Expr errorMessage;

//    @Deprecated
    public Assert_c(Position pos, Expr cond, Expr errorMessage) {
        this(pos, cond, errorMessage, null);
    }

    public Assert_c(Position pos, Expr cond, Expr errorMessage, Ext ext) {
        super(pos, ext);
        assert cond != null; // errorMessage may be null
        this.cond = cond;
        this.errorMessage = errorMessage;
    }

    @Override
    public Expr cond() {
        return cond;
    }

    @Override
    public Assert cond(Expr cond) {
        return cond(this, cond);
    }

    protected <N extends Assert_c> N cond(N n, Expr cond) {
        if (n.cond == cond) return n;
        n = copyIfNeeded(n);
        n.cond = cond;
        return n;
    }

    @Override
    public Expr errorMessage() {
        return errorMessage;
    }

    @Override
    public Assert errorMessage(Expr errorMessage) {
        return errorMessage(this, errorMessage);
    }

    protected <N extends Assert_c> N errorMessage(N n, Expr errorMessage) {
        if (n.errorMessage == errorMessage) return n;
        n = copyIfNeeded(n);
        n.errorMessage = errorMessage;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Assert_c> N reconstruct(N n, Expr cond,
            Expr errorMessage) {
        n = cond(n, cond);
        n = errorMessage(n, errorMessage);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr cond = visitChild(this.cond, v);
        Expr errorMessage = visitChild(this.errorMessage, v);
        return reconstruct(this, cond, errorMessage);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!Options.global.assertions) {
            ErrorQueue eq = tc.errorQueue();
            eq.enqueue(ErrorInfo.WARNING,
                       "assert statements are disabled. Recompile "
                               + "with -assert and ensure the post compiler supports "
                               + "assert (e.g., -post \"javac -source 1.4\"). "
                               + "Removing the statement and continuing.",
                       cond.position());
        }

        if (!ts.typeEquals(cond.type(), ts.Boolean())) {
            throw new SemanticException("Condition of assert statement "
                    + "must have boolean type.", cond.position());
        }

        if (errorMessage != null
                && ts.typeEquals(errorMessage.type(), ts.Void())) {
            throw new SemanticException("Error message in assert statement "
                    + "must have a value.", errorMessage.position());
        }

        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == cond) {
            return ts.Boolean();
        }

        /*
        if (child == errorMessage) {
            return ts.String();
        }
        */

        return child.type();
    }

    @Override
    public String toString() {
        return "assert " + cond.toString()
                + (errorMessage != null ? ": " + errorMessage.toString() : "")
                + ";";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("assert ");
        print(cond, w, tr);

        if (errorMessage != null) {
            w.write(": ");
            print(errorMessage, w, tr);
        }

        w.write(";");
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        if (!Options.global.assertions) {
            w.write(";");
        }
        else {
            tr.lang().prettyPrint(this, w, tr);
        }
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        return Collections.<Type> singletonList(ts.AssertionError());
    }

    @Override
    public Term firstChild() {
        return cond;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitAssert(this);
        if (errorMessage != null) {
            if (v.lang().isConstant(cond, v.lang())) {
                boolean condConstantValue =
                        ((Boolean) v.lang()
                                    .constantValue(cond,
                                                   v.lang())).booleanValue();
                if (condConstantValue) {
                    v.visitCFG(cond, this, EXIT);
                }
                else {
                    v.visitCFG(cond,
                               FlowGraph.EDGE_KEY_FALSE,
                               errorMessage,
                               ENTRY);
                    v.visitCFG(errorMessage, this, EXIT);

                    // AssertionError edge will be handled by visitor.
                    return succs;//Collections.<T> emptyList();
                }
            }
            else {
                v.visitCFG(cond,
                           FlowGraph.EDGE_KEY_TRUE,
                           this,
                           EXIT,
                           FlowGraph.EDGE_KEY_FALSE,
                           errorMessage,
                           ENTRY);
                v.visitCFG(errorMessage, this, EXIT);
            }
        }
        else {
            v.visitCFG(cond, this, EXIT);
        }

        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Assert(position, cond, errorMessage);
    }

}
