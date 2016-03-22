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

import java.util.Iterator;
import java.util.List;

import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An immutable representation of a Java language {@code for}
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public class For_c extends Loop_c implements For {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<ForInit> inits;
    protected List<ForUpdate> iters;

//    @Deprecated
    public For_c(Position pos, List<ForInit> inits, Expr cond,
            List<ForUpdate> iters, Stmt body) {
        this(pos, inits, cond, iters, body, null);
    }

    public For_c(Position pos, List<ForInit> inits, Expr cond,
            List<ForUpdate> iters, Stmt body, Ext ext) {
        super(pos, cond, body, ext);
        assert (inits != null && iters != null); // cond may be null, inits and iters may be empty
        this.inits = ListUtil.copy(inits, true);
        this.iters = ListUtil.copy(iters, true);
    }

    @Override
    public List<ForInit> inits() {
        return this.inits;
    }

    @Override
    public For inits(List<ForInit> inits) {
        return inits(this, inits);
    }

    protected <N extends For_c> N inits(N n, List<ForInit> inits) {
        if (CollectionUtil.equals(n.inits, inits)) return n;
        n = copyIfNeeded(n);
        n.inits = ListUtil.copy(inits, true);
        return n;
    }

    @Override
    public For cond(Expr cond) {
        return cond(this, cond);
    }

    @Override
    public List<ForUpdate> iters() {
        return this.iters;
    }

    @Override
    public For iters(List<ForUpdate> iters) {
        return iters(this, iters);
    }

    protected <N extends For_c> N iters(N n, List<ForUpdate> iters) {
        if (CollectionUtil.equals(n.iters, iters)) return n;
        n = copyIfNeeded(n);
        n.iters = ListUtil.copy(iters, true);
        return n;
    }

    @Override
    public For body(Stmt body) {
        return body(this, body);
    }

    /** Reconstruct the statement. */
    protected <N extends For_c> N reconstruct(N n, List<ForInit> inits,
            Expr cond, List<ForUpdate> iters, Stmt body) {
        n = super.reconstruct(n, cond, body);
        n = inits(n, inits);
        n = iters(n, iters);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<ForInit> inits = visitList(this.inits, v);
        Expr cond = visitChild(this.cond, v);
        List<ForUpdate> iters = visitList(this.iters, v);
        Stmt body = visitChild(this.body, v);
        return reconstruct(this, inits, cond, iters, body);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        // Check that all initializers have the same type.
        // This should be enforced by the parser, but check again here,
        // just to be sure.
        Type t = null;

        for (ForInit s : inits) {
            if (s instanceof LocalDecl) {
                LocalDecl d = (LocalDecl) s;
                Type dt = d.type().type();
                if (t == null) {
                    t = dt;
                }
                else if (!t.typeEquals(dt)) {
                    throw new InternalCompilerError("Local variable "
                                                            + "declarations in a for loop initializer must all "
                                                            + "be the same type, in this case "
                                                            + t + ", not " + dt
                                                            + ".",
                                                    d.position());
                }
            }
        }

        if (cond != null && !ts.isImplicitCastValid(cond.type(), ts.Boolean())) {
            throw new SemanticException("The condition of a for statement must have boolean type.",
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
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("for (");
        w.begin(0);

        if (inits != null) {
            boolean first = true;
            for (Iterator<ForInit> i = inits.iterator(); i.hasNext();) {
                ForInit s = i.next();
                printForInit(s, w, tr, first);
                first = false;

                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(2, " ");
                }
            }
        }

        w.write(";");
        w.allowBreak(0);

        if (cond != null) {
            printBlock(cond, w, tr);
        }

        w.write(";");
        w.allowBreak(0);

        if (iters != null) {
            for (Iterator<ForUpdate> i = iters.iterator(); i.hasNext();) {
                ForUpdate s = i.next();
                printForUpdate(s, w, tr);

                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(2, " ");
                }
            }
        }

        w.end();
        w.write(")");

        printSubStmt(body, w, tr);
    }

    @Override
    public String toString() {
        return "for (...) ...";
    }

    private void printForInit(ForInit s, CodeWriter w, PrettyPrinter tr,
            boolean printType) {
        boolean oldSemiColon = tr.appendSemicolon(false);
        boolean oldPrintType = tr.printType(printType);
        printBlock(s, w, tr);
        tr.printType(oldPrintType);
        tr.appendSemicolon(oldSemiColon);
    }

    private void printForUpdate(ForUpdate s, CodeWriter w, PrettyPrinter tr) {
        boolean oldSemiColon = tr.appendSemicolon(false);
        printBlock(s, w, tr);
        tr.appendSemicolon(oldSemiColon);
    }

    @Override
    public Term firstChild() {
        return listChild(inits, cond != null ? (Term) cond : body);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(inits, cond != null ? (Term) cond : body, ENTRY);

        if (cond != null) {
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
        }

        v.push(this).visitCFG(body, lang().continueTarget(this), ENTRY);
        v.visitCFGList(iters, cond != null ? (Term) cond : body, ENTRY);

        return succs;
    }

    @Override
    public Term continueTarget() {
        return listChild(iters, cond != null ? (Term) cond : body);
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.For(this.position,
                      this.inits,
                      this.cond,
                      this.iters,
                      this.body);
    }

}
