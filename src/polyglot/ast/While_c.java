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
 * An immutable representation of a Java language {@code while}
 * statement.  It contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public class While_c extends Loop_c implements While {
    private static final long serialVersionUID = SerialVersionUID.generate();

//    @Deprecated
    public While_c(Position pos, Expr cond, Stmt body) {
        this(pos, cond, body, null);
    }

    public While_c(Position pos, Expr cond, Stmt body, Ext ext) {
        super(pos, cond, body, ext);
        assert (cond != null);
    }

    @Override
    public While cond(Expr cond) {
        return cond(this, cond);
    }

    @Override
    public While body(Stmt body) {
        return body(this, body);
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr cond = visitChild(this.cond, v);
        Stmt body = visitChild(this.body, v);
        return reconstruct(this, cond, body);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!ts.isImplicitCastValid(cond.type(), ts.Boolean())) {
            throw new SemanticException("Condition of while statement must have boolean type.",
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
        return "while (" + cond + ") ...";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("while (");
        printBlock(cond, w, tr);
        w.write(")");
        printSubStmt(body, w, tr);
    }

    @Override
    public Term firstChild() {
        return cond;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (v.lang().condIsConstantTrue(this, v.lang())) {
            v.visitCFG(cond, body, ENTRY);
        }
        else if (v.lang().condIsConstantFalse(this, v.lang())
                && v.skipDeadLoopBodies()) {
            v.visitCFG(cond, FlowGraph.EDGE_KEY_FALSE, this, EXIT);
            return succs;
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

        v.push(this).visitCFG(body, cond, ENTRY);

        return succs;
    }

    @Override
    public Term continueTarget() {
        return cond;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.While(this.position, this.cond, this.body);
    }

}
