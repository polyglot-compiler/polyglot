package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;

/**
 * A <code>Block</code> represents a Java block statement -- an immutable
 * sequence of statements.
 */
public abstract class AbstractBlock_c extends Stmt_c implements Block
{
    protected List statements;

    public AbstractBlock_c(Ext ext, Position pos, List statements) {
	super(ext, pos);
	this.statements = TypedList.copyAndCheck(statements, Stmt.class, true);
    }

    /** Get the statements of the block. */
    public List statements() {
	return this.statements;
    }

    /** Set the statements of the block. */
    public Block statements(List statements) {
	AbstractBlock_c n = (AbstractBlock_c) copy();
	n.statements = TypedList.copyAndCheck(statements, Stmt.class, true);
	return n;
    }

    /** Append a statement to the block. */
    public Block append(Stmt stmt) {
	List l = new ArrayList(statements.size()+1);
	l.addAll(statements);
	l.add(stmt);
	return statements(l);
    }

    /** Reconstruct the block. */
    protected AbstractBlock_c reconstruct(List statements) {
	if (! CollectionUtil.equals(statements, this.statements)) {
	    AbstractBlock_c n = (AbstractBlock_c) copy();
	    n.statements = TypedList.copyAndCheck(statements, Stmt.class, true);
	    return n;
	}

	return this;
    }

    /** Visit the children of the block. */
    public Node visitChildren(NodeVisitor v) {
	List statements = new ArrayList(this.statements.size());
	for (Iterator i = this.statements.iterator(); i.hasNext(); ) {
	    Stmt n = (Stmt) i.next();
	    n = (Stmt) n.visit(v);
	    statements.add(n);
	}

	return reconstruct(statements);
    }

    public void enterScope(Context c) {
	c.pushBlock();
    }

    public void leaveScope(Context c) {
	c.popBlock();
    }

    /** Write the block to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        enterScope(tr.context());

	w.begin(0);

	for (Iterator i = statements.iterator(); i.hasNext(); ) {
	    Stmt n = (Stmt) i.next();
	    translateBlock(n, w, tr);

	    if (i.hasNext()) {
		w.newline(0);
	    }
	}

	w.end();

	leaveScope(tr.context());
    }
}
