package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>Switch</code> is an immutable representation of a Java
 * <code>switch</code> statement.  Such a statement has an expression which
 * is evaluated to determine where to branch to, an a list of labels
 * and block statements which are conditionally evaluated.  One of the
 * labels, rather than having a constant expression, may be lablled
 * default.
 */
public class Switch_c extends Stmt_c implements Switch
{
    protected Expr expr;
    protected List elements;

    public Switch_c(Del ext, Position pos, Expr expr, List elements) {
	super(ext, pos);
	this.expr = expr;
	this.elements = TypedList.copyAndCheck(elements, SwitchElement.class, true);
    }

    /** Get the expression to switch on. */
    public Expr expr() {
	return this.expr;
    }

    /** Set the expression to switch on. */
    public Switch expr(Expr expr) {
	Switch_c n = (Switch_c) copy();
	n.expr = expr;
	return n;
    }

    /** Get the switch elements of the statement. */
    public List elements() {
	return Collections.unmodifiableList(this.elements);
    }

    /** Set the switch elements of the statement. */
    public Switch elements(List elements) {
	Switch_c n = (Switch_c) copy();
	n.elements = TypedList.copyAndCheck(elements, SwitchElement.class, true);
	return n;
    }

    /** Reconstruct the statement. */
    protected Switch_c reconstruct(Expr expr, List elements) {
	if (expr != this.expr || ! CollectionUtil.equals(elements, this.elements)) {
	    Switch_c n = (Switch_c) copy();
	    n.expr = expr;
	    n.elements = TypedList.copyAndCheck(elements, SwitchElement.class, true);
	    return n;
	}

	return this;
    }

    /** Visit the children of the statement. */
    public Node visitChildren(NodeVisitor v) {
	Expr expr = (Expr) visitChild(this.expr, v);
	List elements = visitList(this.elements, v);
	return reconstruct(expr, elements);
    }

    /** Type check the statement. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (! expr.type().isImplicitCastValid(ts.Int())) {
            throw new SemanticException("Switch index must be an integer.",
                                        position());
        }

        Collection labels = new HashSet();

	for (Iterator i = elements.iterator(); i.hasNext();) {
	   SwitchElement s = (SwitchElement) i.next();

	   if (s instanceof Case) {
	       Case c = (Case) s;
	       Object key;
	       String str;

	       if (c.isDefault()) {
		   key = "default";
		   str = "default";
	       }
	       else {
		   key = new Long(c.value());
		   str = c.expr().toString() + " (" + c.value() + ")";
	       }

	       if (labels.contains(key)) {
		   throw new SemanticException("Duplicate case label: " +
		       str + ".", c.position());
	       }

	       labels.add(key);
	   }
	}

	return this;
    }

    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == expr) {
            return child.expectedType(ts.Int());
        }

        return child;
    }

    public String toString() {
	return "switch (" + expr + ") { ... }";
    }

    /** Write the statement to an output file. */
    public void translate(CodeWriter w, Translator tr) {
	w.write("switch (");
	translateBlock(expr, w, tr);
	w.write(") {");
	w.allowBreak(4, " ");
	w.begin(0);

	for (Iterator i = elements.iterator(); i.hasNext();) {
	   SwitchElement s = (SwitchElement) i.next();
	   s.del().translate(w, tr);
	   if (i.hasNext()) {
	       w.newline(0);
	   }
	}

	w.end();
	w.newline(0);
	w.write("}");
    }
}
