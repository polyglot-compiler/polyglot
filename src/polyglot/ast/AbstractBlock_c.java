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

import polyglot.types.*;
import polyglot.util.*;
import polyglot.util.CodeWriter;
import polyglot.visit.*;

import java.util.*;

/**
 * A <code>Block</code> represents a Java block statement -- an immutable
 * sequence of statements.
 */
public abstract class AbstractBlock_c extends Stmt_c implements Block
{
    protected List<Stmt> statements;

    public AbstractBlock_c(Position pos, List statements) {
	super(pos);
	assert(statements != null);
	this.statements = ListUtil.copy(statements, true);
    }

    /** Get the statements of the block. */
    public List statements() {
	return this.statements;
    }

    /** Set the statements of the block. */
    public Block statements(List statements) {
	AbstractBlock_c n = (AbstractBlock_c) copy();
	n.statements = ListUtil.copy(statements, true);
	return n;
    }

    /** Append a statement to the block. */
    public Block append(Stmt stmt) {
	List l = new ArrayList(statements.size()+1);
	l.addAll(statements);
	l.add(stmt);
	return statements(l);
    }

    /** Prepend a statement to the block. */
    public Block prepend(Stmt stmt) {
        List l = new ArrayList(statements.size()+1);
        l.add(stmt);
        l.addAll(statements);
        return statements(l);
    }

    /** Reconstruct the block. */
    protected AbstractBlock_c reconstruct(List statements) {
	if (! CollectionUtil.equals(statements, this.statements)) {
	    AbstractBlock_c n = (AbstractBlock_c) copy();
	    n.statements = ListUtil.copy(statements, true);
	    return n;
	}

	return this;
    }

    /** Visit the children of the block. */
    public Node visitChildren(NodeVisitor v) {
        List statements = visitList(this.statements, v);
	return reconstruct(statements);
    }

    public Context enterScope(Context c) {
	return c.pushBlock();
    }

    /** Write the block to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.begin(0);

	for (Iterator i = statements.iterator(); i.hasNext(); ) {
	    Stmt n = (Stmt) i.next();
	    printBlock(n, w, tr);

	    if (i.hasNext()) {
		w.newline();
	    }
	}

	w.end();
    }

    public Term firstChild() {
        return listChild(statements, null);
    }

    public List acceptCFG(CFGBuilder v, List succs) {
        v.visitCFGList(statements, this, EXIT);
        return succs;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");

        int count = 0;

        for (Iterator i = statements.iterator(); i.hasNext(); ) {
            if (count++ > 2) {
                sb.append(" ...");
                break;
            }

            Stmt n = (Stmt) i.next();
            sb.append(" ");
            sb.append(n.toString());
        }

        sb.append(" }");
        return sb.toString();
    }
}
