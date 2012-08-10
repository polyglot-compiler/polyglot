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
 * An immutable representation of a Java language <code>if</code> statement.
 * Contains an expression whose value is tested, a ``then'' statement 
 * (consequent), and optionally an ``else'' statement (alternate).
 */
public class If_c extends Stmt_c implements If {
    protected Expr cond;
    protected Stmt consequent;
    protected Stmt alternative;

    public If_c(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
        super(pos);
        assert (cond != null && consequent != null); // alternative may be null;
        this.cond = cond;
        this.consequent = consequent;
        this.alternative = alternative;
    }

    /** Get the conditional of the statement. */
    @Override
    public Expr cond() {
        return this.cond;
    }

    /** Set the conditional of the statement. */
    @Override
    public If cond(Expr cond) {
        If_c n = (If_c) copy();
        n.cond = cond;
        return n;
    }

    /** Get the consequent of the statement. */
    @Override
    public Stmt consequent() {
        return this.consequent;
    }

    /** Set the consequent of the statement. */
    @Override
    public If consequent(Stmt consequent) {
        If_c n = (If_c) copy();
        n.consequent = consequent;
        return n;
    }

    /** Get the alternative of the statement. */
    @Override
    public Stmt alternative() {
        return this.alternative;
    }

    /** Set the alternative of the statement. */
    @Override
    public If alternative(Stmt alternative) {
        If_c n = (If_c) copy();
        n.alternative = alternative;
        return n;
    }

    /** Reconstruct the statement. */
    protected If_c reconstruct(Expr cond, Stmt consequent, Stmt alternative) {
        if (cond != this.cond || consequent != this.consequent
                || alternative != this.alternative) {
            If_c n = (If_c) copy();
            n.cond = cond;
            n.consequent = consequent;
            n.alternative = alternative;
            return n;
        }

        return this;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr cond = (Expr) visitChild(this.cond, v);
        Node consequent = visitChild(this.consequent, v);
        Node alternative = visitChild(this.alternative, v);
        return reconstruct(cond, (Stmt) consequent, (Stmt) alternative);
    }

    /** Type check the statement. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!ts.isImplicitCastValid(cond.type(), ts.Boolean())) {
            throw new SemanticException("Condition of if statement must have boolean type.",
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
        return "if (" + cond + ") " + consequent
                + (alternative != null ? " else " + alternative : "");
    }

    /** Write the statement to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("if (");
        printBlock(cond, w, tr);
        w.write(")");

        printSubStmt(consequent, w, tr);

        if (alternative != null) {
            if (consequent instanceof Block) {
                // allow the "} else {" formatting except in emergencies
                w.allowBreak(0, 2, " ", 1);
            }
            else {
                w.allowBreak(0, " ");
            }

            if (alternative instanceof Block) {
                w.write("else ");
                print(alternative, w, tr);
            }
            else {
                w.write("else");
                printSubStmt(alternative, w, tr);
            }
        }
    }

    @Override
    public Term firstChild() {
        return cond;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (alternative == null) {
            v.visitCFG(cond,
                       FlowGraph.EDGE_KEY_TRUE,
                       consequent,
                       ENTRY,
                       FlowGraph.EDGE_KEY_FALSE,
                       this,
                       EXIT);
            v.visitCFG(consequent, this, EXIT);
        }
        else {
            v.visitCFG(cond,
                       FlowGraph.EDGE_KEY_TRUE,
                       consequent,
                       ENTRY,
                       FlowGraph.EDGE_KEY_FALSE,
                       alternative,
                       ENTRY);
            v.visitCFG(consequent, this, EXIT);
            v.visitCFG(alternative, this, EXIT);
        }

        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.If(this.position,
                     this.cond,
                     this.consequent,
                     this.alternative);
    }

}
