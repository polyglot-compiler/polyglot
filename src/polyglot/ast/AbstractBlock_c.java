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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.types.Context;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A <code>Block</code> represents a Java block statement -- an immutable
 * sequence of statements.
 */
public abstract class AbstractBlock_c extends Stmt_c implements Block {
    protected List<Stmt> statements;

    public AbstractBlock_c(Position pos, List<Stmt> statements) {
        super(pos);
        assert (statements != null);
        this.statements = ListUtil.copy(statements, true);
    }

    /** Get the statements of the block. */
    @Override
    public List<Stmt> statements() {
        return this.statements;
    }

    /** Set the statements of the block. */
    @Override
    public Block statements(List<Stmt> statements) {
        AbstractBlock_c n = (AbstractBlock_c) copy();
        n.statements = ListUtil.copy(statements, true);
        return n;
    }

    /** Append a statement to the block. */
    @Override
    public Block append(Stmt stmt) {
        List<Stmt> l = new ArrayList<Stmt>(statements.size() + 1);
        l.addAll(statements);
        l.add(stmt);
        return statements(l);
    }

    /** Prepend a statement to the block. */
    @Override
    public Block prepend(Stmt stmt) {
        List<Stmt> l = new ArrayList<Stmt>(statements.size() + 1);
        l.add(stmt);
        l.addAll(statements);
        return statements(l);
    }

    /** Reconstruct the block. */
    protected AbstractBlock_c reconstruct(List<Stmt> statements) {
        if (!CollectionUtil.equals(statements, this.statements)) {
            AbstractBlock_c n = (AbstractBlock_c) copy();
            n.statements = ListUtil.copy(statements, true);
            return n;
        }

        return this;
    }

    /** Visit the children of the block. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Stmt> statements = visitList(this.statements, v);
        return reconstruct(statements);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

    /** Write the block to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);

        for (Iterator<Stmt> i = statements.iterator(); i.hasNext();) {
            Stmt n = i.next();
            printBlock(n, w, tr);

            if (i.hasNext()) {
                w.newline();
            }
        }

        w.end();
    }

    @Override
    public Term firstChild() {
        return listChild(statements, (Stmt) null);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(statements, this, EXIT);
        return succs;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");

        int count = 0;

        for (Stmt n : statements) {
            if (count++ > 2) {
                sb.append(" ...");
                break;
            }

            sb.append(" ");
            sb.append(n.toString());
        }

        sb.append(" }");
        return sb.toString();
    }
}
