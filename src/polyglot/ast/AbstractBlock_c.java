package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/**
 * A <code>Block</code> represents a Java block statement -- an immutable
 * sequence of statements.
 */
public abstract class AbstractBlock_c extends Stmt_c implements Block
{
    protected List statements;

    public AbstractBlock_c(Del ext, Position pos, List statements) {
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
	    n.statements = TypedList.copyAndCheck(statements, Stmt.class, true);
	    return n;
	}

	return this;
    }

    /** Visit the children of the block. */
    public Node visitChildren(NodeVisitor v) {
        List statements = visitList(this.statements, v);
	return reconstruct(statements);
    }

    public void enterScope(Context c) {
	c.pushBlock();
    }

    public void leaveScope(Context c) {
	c.popBlock();
    }

    /** Write the block to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.begin(0);

	for (Iterator i = statements.iterator(); i.hasNext(); ) {
	    Stmt n = (Stmt) i.next();
	    printBlock(n, w, tr);

	    if (i.hasNext()) {
		w.newline(0);
	    }
	}

	w.end();
    }

    public void translate(CodeWriter w, Translator tr) {
        enterScope(tr.context());
        super.translate(w, tr);
	leaveScope(tr.context());
    }
}
