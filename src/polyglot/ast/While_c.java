package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An immutable representation of a Java language <code>while</code>
 * statement.  It contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */ 
public class While_c extends Stmt_c implements While
{
    protected Expr cond;
    protected Stmt body;

    public While_c(Del ext, Position pos, Expr cond, Stmt body) {
	super(ext, pos);
	this.cond = cond;
	this.body = body;
    }

    /** Get the conditional of the statement. */
    public Expr cond() {
	return this.cond;
    }

    /** Set the conditional of the statement. */
    public While cond(Expr cond) {
	While_c n = (While_c) copy();
	n.cond = cond;
	return n;
    }

    /** Get the body of the statement. */
    public Stmt body() {
	return this.body;
    }

    /** Set the body of the statement. */
    public While body(Stmt body) {
	While_c n = (While_c) copy();
	n.body = body;
	return n;
    }

    /** Reconstruct the statement. */
    protected While_c reconstruct(Expr cond, Stmt body) {
	if (cond != this.cond || body != this.body) {
	    While_c n = (While_c) copy();
	    n.cond = cond;
	    n.body = body;
	    return n;
	}

	return this;
    }

    /** Visit the children of the statement. */
    public Node visitChildren(NodeVisitor v) {
	Expr cond = (Expr) visitChild(this.cond, v);
	Stmt body = (Stmt) visitChild(this.body, v);
	return reconstruct(cond, body);
    }

    /** Type check the statement. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();
	
	if (! cond.type().isSame(ts.Boolean())) {
	    throw new SemanticException(
		"Condition of while statement must have boolean type.",
		cond.position());
	}
	
	return this;
    }

    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == cond) {
            return child.expectedType(ts.Boolean());
        }

        return child;
    }

    public String toString() {
	return "while (" + cond + ") ...";
    }

    /** Write the statement to an output file. */
    public void translate(CodeWriter w, Translator tr) {
	w.write("while (");
	translateBlock(cond, w, tr);
	w.write(")");
	translateSubstmt(body, w, tr);

	// FIXME: we used to construct with null body for while (c) ;
	// check the creators of While nodes.
    }
}
