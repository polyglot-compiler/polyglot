package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>Switch</code> is an immutable representation of a Java
 * <code>swtich</code> statement.  Such a statement has an expression which
 * is evaluated to determine where to branch to, an a list of labels
 * and block statements which are conditionally evaluated.  One of the
 * labels, rather than having a constant expression, may be lablled
 * default.
 */
public class Switch_c extends Stmt_c implements Switch
{
    protected Expr expr;
    protected List elements;

    public Switch_c(Ext ext, Position pos, Expr expr, List elements) {
	super(ext, pos);
	this.expr = expr;
	this.elements = TypedList.copyAndCheck(elements, SwitchElement.class, true);
    }

    public Expr expr() {
	return this.expr;
    }

    public Switch expr(Expr expr) {
	Switch_c n = (Switch_c) copy();
	n.expr = expr;
	return n;
    }

    public List elements() {
	return Collections.unmodifiableList(this.elements);
    }

    public Switch elements(List elements) {
	Switch_c n = (Switch_c) copy();
	n.elements = TypedList.copyAndCheck(elements, SwitchElement.class, true);
	return n;
    }

    protected Switch_c reconstruct(Expr expr, List elements) {
	if (expr != this.expr || ! CollectionUtil.equals(elements, this.elements)) {
	    Switch_c n = (Switch_c) copy();
	    n.expr = expr;
	    n.elements = TypedList.copyAndCheck(elements, SwitchElement.class, true);
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Expr expr = (Expr) this.expr.visit(v);

	List elements = new ArrayList(this.elements.size());
	for (Iterator i = this.elements.iterator(); i.hasNext(); ) {
	    SwitchElement n = (SwitchElement) i.next();
	    n = (SwitchElement) n.visit(v);
	    elements.add(n);
	}

	return reconstruct(expr, elements);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
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

    public String toString() {
	return "switch (" + expr + ") { ... }";
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write("switch (");
	translateBlock(expr, w, tr);
	w.write(") {");
	w.allowBreak(4, " ");
	w.begin(0);

	for (Iterator i = elements.iterator(); i.hasNext();) {
	   SwitchElement s = (SwitchElement) i.next();
	   s.ext().translate(w, tr);
	   if (i.hasNext()) {
	       w.newline(0);
	   }
	}

	w.end();
	w.newline(0);
	w.write("}");
    }
}
