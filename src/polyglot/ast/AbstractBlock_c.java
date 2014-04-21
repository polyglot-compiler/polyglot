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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.types.Context;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A {@code Block} represents a Java block statement -- an immutable
 * sequence of statements.
 */
public abstract class AbstractBlock_c extends Stmt_c implements Block {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<Stmt> statements;

    @Deprecated
    public AbstractBlock_c(Position pos, List<Stmt> statements) {
        this(pos, statements, null);
    }

    public AbstractBlock_c(Position pos, List<Stmt> statements, Ext ext) {
        super(pos, ext);
        assert (statements != null);
        this.statements = ListUtil.copy(statements, true);
    }

    @Override
    public List<Stmt> statements() {
        return this.statements;
    }

    @Override
    public Block statements(List<Stmt> statements) {
        return statements(this, statements);
    }

    protected <N extends AbstractBlock_c> N statements(N n,
            List<Stmt> statements) {
        if (CollectionUtil.equals(n.statements, statements)) return n;
        n = copyIfNeeded(n);
        n.statements = ListUtil.copy(statements, true);
        return n;
    }

    @Override
    public Block append(Stmt stmt) {
        List<Stmt> l = new ArrayList<>(statements.size() + 1);
        l.addAll(statements);
        l.add(stmt);
        return statements(l);
    }

    @Override
    public Block prepend(Stmt stmt) {
        List<Stmt> l = new ArrayList<>(statements.size() + 1);
        l.add(stmt);
        l.addAll(statements);
        return statements(l);
    }

    /** Reconstruct the block. */
    protected <N extends AbstractBlock_c> N reconstruct(N n,
            List<Stmt> statements) {
        n = statements(n, statements);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Stmt> statements = visitList(this.statements, v);
        return reconstruct(this, statements);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

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
