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

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A immutable representation of a Java language <code>do</code> statement. 
 * It contains a statement to be executed and an expression to be tested 
 * indicating whether to reexecute the statement.
 */
public class Do_c extends Loop_c implements Do {
    protected Stmt body;
    protected Expr cond;

    public Do_c(Position pos, Stmt body, Expr cond) {
        super(pos);
        assert (body != null && cond != null);
        this.body = body;
        this.cond = cond;
    }

    /** Get the body of the statement. */
    @Override
    public Stmt body() {
        return this.body;
    }

    /** Set the body of the statement. */
    @Override
    public Do body(Stmt body) {
        Do_c n = (Do_c) copy();
        n.body = body;
        return n;
    }

    /** Get the conditional of the statement. */
    @Override
    public Expr cond() {
        return this.cond;
    }

    /** Set the conditional of the statement. */
    @Override
    public Do cond(Expr cond) {
        Do_c n = (Do_c) copy();
        n.cond = cond;
        return n;
    }

    /** Reconstruct the statement. */
    protected Do_c reconstruct(Stmt body, Expr cond) {
        if (body != this.body || cond != this.cond) {
            Do_c n = (Do_c) copy();
            n.body = body;
            n.cond = cond;
            return n;
        }

        return this;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Stmt body = (Stmt) visitChild(this.body, v);
        Expr cond = (Expr) visitChild(this.cond, v);
        return reconstruct(body, cond);
    }

    /** Type check the statement. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!ts.isImplicitCastValid(cond.type(), ts.Boolean())) {
            throw new SemanticException("Condition of do statement must have boolean type.",
                                        cond.position());
        }

        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == cond) {
            return ts.Boolean();
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "do { ... } while (" + cond + ")";
    }

    /** Write the statement to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("do ");
        printSubStmt(body, w, tr);
        w.write("while(");
        printBlock(cond, w, tr);
        w.write("); ");
    }

    @Override
    public Term firstChild() {
        return body;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.push(this).visitCFG(body, cond, ENTRY);

        if (condIsConstantTrue()) {
            v.visitCFG(cond, body, ENTRY);
        }
        else {
            v.visitCFG(cond,
                       FlowGraph.EDGE_KEY_TRUE,
                       body,
                       ENTRY,
                       FlowGraph.EDGE_KEY_FALSE,
                       this,
                       EXIT);
        }

        return succs;
    }

    @Override
    public Term continueTarget() {
        return cond;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Do(this.position, this.body, this.cond);
    }

}
