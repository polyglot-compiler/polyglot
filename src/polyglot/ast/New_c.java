package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;

/**
 * A <code>New</code> is an immutable representation of the use of the
 * <code>new</code> operator to create a new instance of a class.  In
 * addition to the type of the class being created, a <code>New</code> has a
 * list of arguments to be passed to the constructor of the object and an
 * optional <code>ClassBody</code> used to support anonymous classes.
 */
public class New_c extends AbstractNew_c implements New
{
    protected TypeNode tn;

    public New_c(Ext ext, Position pos, TypeNode tn, List arguments, ClassBody body) {
	super(ext, pos, arguments, body);
	this.tn = tn;
    }

    /** Get the type we are instantiating. */
    public TypeNode objectType() {
        return this.tn;
    }

    /** Set the type we are instantiating. */
    public New objectType(TypeNode tn) {
        New_c n = (New_c) copy();
	n.tn = tn;
	return n;
    }

    /** Set the arguments of the expression. */
    public New arguments(List arguments) {
        return (New) setArguments(arguments);
    }

    /** Set the body of the expression. */
    public New body(ClassBody body) {
        return (New) setBody(body);
    }

    /** Reconstruct the expression. */
    protected New_c reconstruct(TypeNode tn, List arguments, ClassBody body) {
	if (tn != this.tn || ! CollectionUtil.equals(arguments, this.arguments) || body != this.body) {
	    New_c n = (New_c) copy();
	    n.tn = tn;
	    n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
	    n.body = body;
	    return n;
	}

	return this;
    }

    /** Visit the children of the expression, except the body. */
    protected AbstractNew_c visitNonBodyChildren(NodeVisitor v) {
	TypeNode tn = (TypeNode) this.tn.visit(v);

	List arguments = new ArrayList(this.arguments.size());
	for (Iterator i = this.arguments.iterator(); i.hasNext(); ) {
	    Expr n = (Expr) i.next();
	    n = (Expr) n.visit(v);
	    arguments.add(n);
	}

	return reconstruct(tn, arguments, this.body);
    }

    /** Type check the expression. */
    public Node typeCheckOverride_(TypeChecker tc) throws SemanticException {
        New_c n = (New_c) visitNonBodyChildren(tc);
	ClassType ct = (ClassType) n.tn.type();
	return n.typeCheckEpilogue(ct, tc);
    }

    public String toString() {
	return "new " + tn + "(...)" + (body != null ? " " + body : "");
    }

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	w.write("new ");
	tn.ext().translate(w, tr);
	translateEpilogue(w, tr);
    }
}
