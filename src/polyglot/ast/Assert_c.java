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

import polyglot.main.Options;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

/**
 * An <code>Assert</code> is an assert statement.
 */
public class Assert_c extends Stmt_c implements Assert {
    protected Expr cond;
    protected Expr errorMessage;

    public Assert_c(Position pos, Expr cond, Expr errorMessage) {
        super(pos);
        assert (cond != null); // errorMessage may be null
        this.cond = cond;
        this.errorMessage = errorMessage;
    }

    /** Get the condition to check. */
    @Override
    public Expr cond() {
        return this.cond;
    }

    /** Set the condition to check. */
    @Override
    public Assert cond(Expr cond) {
        Assert_c n = (Assert_c) copy();
        n.cond = cond;
        return n;
    }

    /** Get the error message to report. */
    @Override
    public Expr errorMessage() {
        return this.errorMessage;
    }

    /** Set the error message to report. */
    @Override
    public Assert errorMessage(Expr errorMessage) {
        Assert_c n = (Assert_c) copy();
        n.errorMessage = errorMessage;
        return n;
    }

    /** Reconstruct the statement. */
    protected Assert_c reconstruct(Expr cond, Expr errorMessage) {
        if (cond != this.cond || errorMessage != this.errorMessage) {
            Assert_c n = (Assert_c) copy();
            n.cond = cond;
            n.errorMessage = errorMessage;
            return n;
        }

        return this;
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

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr cond = (Expr) visitChild(this.cond, v);
        Expr errorMessage = (Expr) visitChild(this.errorMessage, v);
        return reconstruct(cond, errorMessage);
    }

    @Override
    public String toString() {
        return "assert " + cond.toString()
                + (errorMessage != null ? ": " + errorMessage.toString() : "")
                + ";";
    }

    /** Write the statement to an output file. */
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
            this.del().prettyPrint(w, tr);
        }
    }

    @Override
    public Term firstChild() {
        return cond;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (errorMessage != null) {
            v.visitCFG(cond, errorMessage, ENTRY);
            v.visitCFG(errorMessage, this, EXIT);
        }
        else {
            v.visitCFG(cond, this, EXIT);
        }

        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Assert(this.position, this.cond, this.errorMessage);
    }

}
