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
    public Node visitChildren(NodeVisitor v) {
	TypeNode tn = (TypeNode) visitChild(this.tn, v);
	List arguments = visitList(this.arguments, v);
	ClassBody body = (ClassBody) visitChild(this.body, v);
	return reconstruct(tn, arguments, body);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	ClassType ct = (ClassType) tn.type();

        /*
        if (ct.isMember()) {
            ClassType currentClass = tc.context().currentClass();

            for (ClassType t = ct; t.isMember(); t = t.toMember().outer()) {
                if (! t.flags().isStatic()) {
                    throw new SemanticException("Cannot allocate non-static " +
                                                "member class \"" + t + "\".");
                }
            }
        }
        */

	return typeCheckEpilogue(ct, tc);
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
