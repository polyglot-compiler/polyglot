package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;

/**
 * A <code>Catch</code> represents one half of a <code>try... catch</code>
 * statement. Specifically, the second half. The example below demonstrates a
 * catch block with parameter <code>ioe</code> of type <code>IOException</code>
 * that prints out the stack trace of the exception.
 * <pre><code>
 * ...
 * catch( IOException ioe) 
 * {
 *   ioe.printStackTrace();
 * }
 * </code></pre>
 */
public class Catch_c extends Stmt_c implements Catch
{
    protected Formal formal;
    protected Block body;

    public Catch_c(Ext ext, Position pos, Formal formal, Block body) {
	super(ext, pos);
	this.formal = formal;
	this.body = body;
    }

    public Type catchType() {
        return formal.declType();
    }

    public Formal formal() {
	return this.formal;
    }

    public Catch formal(Formal formal) {
	Catch_c n = (Catch_c) copy();
	n.formal = formal;
	return n;
    }

    public Block body() {
	return this.body;
    }

    public Catch body(Block body) {
	Catch_c n = (Catch_c) copy();
	n.body = body;
	return n;
    }

    protected Catch_c reconstruct(Formal formal, Block body) {
	if (formal != this.formal || body != this.body) {
	    Catch_c n = (Catch_c) copy();
	    n.formal = formal;
	    n.body = body;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Formal formal = (Formal) this.formal.visit(v);
	Block body = (Block) this.body.visit(v);
	return reconstruct(formal, body);
    }

    public void enterScope(Context c) {
      c.pushBlock();
    }

    public void leaveScope(Context c) {
      c.popBlock();
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	if (! catchType().isThrowable()) {
	    throw new SemanticException(
		"Can only throw subclasses of \"" +
		ts.Throwable() + "\".", formal.position());

	}

	return this;
    }

    public String toString() {
	return "catch (" + formal + ") " + body;
    }

    public void translate_(CodeWriter w, Translator tr) {
        Context c = tr.context();

	w.write("catch(");
	translateBlock(formal, w, tr);
	w.write(")");

	enterScope(c);
	translateSubstmt(body, w, tr);
	leaveScope(c);
    }
}
