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

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An immutable representation of a Java language {@code if} statement.
 * Contains an expression whose value is tested, a ``then'' statement
 * (consequent), and optionally an ``else'' statement (alternate).
 */
public class If_c extends Stmt_c implements If {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr cond;
    protected Stmt consequent;
    protected Stmt alternative;

//    @Deprecated
    public If_c(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
        this(pos, cond, consequent, alternative, null);
    }

    public If_c(Position pos, Expr cond, Stmt consequent, Stmt alternative,
            Ext ext) {
        super(pos, ext);
        assert cond != null && consequent != null; // alternative may be null;
        this.cond = cond;
        this.consequent = consequent;
        this.alternative = alternative;
    }

    @Override
    public Expr cond() {
        return cond;
    }

    @Override
    public If cond(Expr cond) {
        return cond(this, cond);
    }

    protected <N extends If_c> N cond(N n, Expr cond) {
        if (n.cond == cond) return n;
        n = copyIfNeeded(n);
        n.cond = cond;
        return n;
    }

    @Override
    public Stmt consequent() {
        return consequent;
    }

    @Override
    public If consequent(Stmt consequent) {
        return consequent(this, consequent);
    }

    protected <N extends If_c> N consequent(N n, Stmt consequent) {
        if (n.consequent == consequent) return n;
        n = copyIfNeeded(n);
        n.consequent = consequent;
        return n;
    }

    @Override
    public Stmt alternative() {
        return alternative;
    }

    @Override
    public If alternative(Stmt alternative) {
        return alternative(this, alternative);
    }

    protected <N extends If_c> N alternative(N n, Stmt alternative) {
        if (n.alternative == alternative) return n;
        n = copyIfNeeded(n);
        n.alternative = alternative;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends If_c> N reconstruct(N n, Expr cond, Stmt consequent,
            Stmt alternative) {
        n = cond(n, cond);
        n = consequent(n, consequent);
        n = alternative(n, alternative);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr cond = visitChild(this.cond, v);
        Stmt consequent = visitChild(this.consequent, v);
        Stmt alternative = visitChild(this.alternative, v);
        return reconstruct(this, cond, consequent, alternative);
    }

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

            w.write("else");
            if (alternative instanceof If) {
                w.write(" ");
                print(alternative, w, tr);
            }
            else printSubStmt(alternative, w, tr);
        }
    }

    @Override
    public Term firstChild() {
        return cond;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (v.lang().isConstant(cond, v.lang()) && v.skipDeadIfBranches()) {
            // the condition is a constant expression.
            // That means that one branch is dead code
            boolean condConstantValue =
                    ((Boolean) v.lang().constantValue(cond,
                                                      v.lang())).booleanValue();
            if (condConstantValue) {
                // the condition is constantly true.
                // the alternative won't be executed.
                v.visitCFG(cond, FlowGraph.EDGE_KEY_TRUE, consequent, ENTRY);
                v.visitCFG(consequent, this, EXIT);
            }
            else {
                // the condition is constantly false.
                // the consequent won't be executed.
                if (alternative == null) {
                    // there is no alternative
                    v.visitCFG(cond, this, EXIT);
                }
                else {
                    v.visitCFG(cond,
                               FlowGraph.EDGE_KEY_FALSE,
                               alternative,
                               ENTRY);
                    v.visitCFG(alternative, this, EXIT);
                }
            }
        }
        else if (alternative == null) {
            // the alternative is null (but the condition is not constant, or we can't
            // skip dead statements.)
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
            // both consequent and alternative are present, and either the condition
            // is not constant or we can't skip dead statements.
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
        return nf.If(position, cond, consequent, alternative);
    }

}
