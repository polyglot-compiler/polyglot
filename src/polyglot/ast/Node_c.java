package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

import java.io.*;
import java.util.*;

/**
 * A <code>Node</code> represents an AST node.  All AST nodes must implement
 * this interface.  Nodes should be immutable: methods which set fields
 * of the node should copy the node, set the field in the copy, and then
 * return the copy.
 */
public abstract class Node_c implements Node
{
    protected Position position;
    protected Del del;

    public Node_c(Del del, Position pos) {
        this.del = del;
        this.del.init(this);
	this.position = pos;
    }

    public Del del() {
        return del;
    }

    public Node del(Del del) {
        if (this.del == del) {
            return this;
        }

        try {
            // use clone here, not copy to avoid copying the del as well
            Node_c n = (Node_c) super.clone();
            n.del = del;
            n.del.init(n);
            return n;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    public Object copy() {
        return del((Del) this.del.copy());
    }

    public Position position() {
	return this.position;
    }

    public Node position(Position position) {
	Node_c n = (Node_c) copy();
	n.position = position;
	return n;
    }

    public Node visitChild(Node n, NodeVisitor v) {
	if (n == null) {
	    return null;
	}

	return n.visitEdge(this, v);
    }

    public Node visit(NodeVisitor v) {
	return visitEdge(null, v);
    }

    public Node visitEdge(Node parent, NodeVisitor v) {
	Node n = v.override(parent, this);

	if (n == null) {
	    NodeVisitor v_ = v.enter(parent, this);

	    if (v_ == null) {
		throw new InternalCompilerError(
		    "NodeVisitor.enter() returned null.");
	    }

	    n = this.visitChildren(v_);

	    if (n == null) {
		throw new InternalCompilerError(
		    "Node_c.visitChildren() returned null.");
	    }

	    n = v.leave(parent, this, n, v_);

	    if (n == null) {
		throw new InternalCompilerError(
		    "NodeVisitor.leave() returned null.");
	    }
	}

	return n;
    }

    /**
     * Visit all the elements of a list.
     * @param l The list to visit.
     * @param v The visitor to use.
     * @return A new list with each element from the old list
     *         replaced by the result of visiting that element.
     *         If <code>l</code> is a <code>TypedList</code>, the
     *         new list will also be typed with the same type as 
     *         <code>l</code>.  If <code>l</code> is <code>null</code>,
     *         <code>null</code> is returned.
     */
    public List visitList(List l, NodeVisitor v) {
	if (l == null) {
	    return null;
	}
	
	List vl = makeVisitedList(l);
	for (Iterator i = l.iterator(); i.hasNext(); ) {
	    Node n = (Node) i.next();
	    n = visitChild(n, v);
	    vl.add(n);
	}
	return vl;
    }
    
    /**
     * Helper method for visitList().
     * @return A List of capacity the same as the size of <code>l</code>, 
     *             and also typed the same as <code>l</code>, if <code>l</code>
     *         is a TypedList.
     */
    private static List makeVisitedList(List l) {
	ArrayList a = new ArrayList(l.size());
	if (l instanceof TypedList) {
	    TypedList t = (TypedList) l;
	    return new TypedList(a, t.getAllowedType(), false);
	} else {
	    return a;
	}
    }
    
    public Node visitChildren(NodeVisitor v) {
	return this;
    }

    /** Adjust the environment for entering a new scope. */
    public Context enterScope(Context c) { return c; }

    // These methods override the methods in Ext_c.
    // These are the default implementation of these passes.

    public Node buildTypesOverride(TypeBuilder tb) throws SemanticException {
	return null;
    }

    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
	return tb;
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
	return this;
    }

    /** Remove any remaining ambiguities from the AST. */
    public Node disambiguateOverride(AmbiguityRemover ar) throws SemanticException {
	return null;
    }

    public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
	return ar;
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	return this;
    }

    /** Add members to a class. */
    public Node addMembersOverride(AddMemberVisitor am) throws SemanticException {
	return null;
    }

    public NodeVisitor addMembersEnter(AddMemberVisitor am) throws SemanticException {
	return am;
    }

    public Node addMembers(AddMemberVisitor am) throws SemanticException {
	return this;
    }

    /** Fold all constants. */
    public Node foldConstantsOverride(ConstantFolder cf) {
	return null;
    }

    public NodeVisitor foldConstantsEnter(ConstantFolder cf) {
	return cf;
    }

    public Node foldConstants(ConstantFolder cf) {
	return this;
    }

    /** Type check the AST. */
    public Node typeCheckOverride(TypeChecker tc) throws SemanticException {
	return null;
    }

    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
	return tc;
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
	return this;
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av) {
	return child.type();
    }

    /** Check that exceptions are properly propagated throughout the AST. */
    public Node exceptionCheckOverride(ExceptionChecker ec) throws SemanticException {
	return null;
    }

    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec) throws SemanticException {
	return ec;
    }

    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	return this;
    }

    /** Pretty-print the AST using the given <code>CodeWriter</code>. */
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) { }

    public void printBlock(Node n, CodeWriter w, PrettyPrinter pp) {
            w.begin(0);
            pp.print(n, w);
            w.end();
    }

    public void printSubStmt(Stmt stmt, CodeWriter w, PrettyPrinter pp) {
            if (stmt instanceof Block) {
                w.write(" ");
                pp.print(stmt, w);
            }
            else {
                w.allowBreak(4, " ");
                printBlock(stmt, w, pp);
            }
    }

    /** Translate the AST using the given <code>CodeWriter</code>. */
    public void translate(CodeWriter w, Translator tr) {
            // By default, just rely on the pretty printer.
            this.del().prettyPrint(w, tr.context(enterScope(tr.context())));
    }

    public void dump(CodeWriter w) {
            w.write(StringUtil.getShortNameComponent(getClass().getName()));

            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(del " + del() + ")");
            w.end();

            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(position " + (position != null ? position.toString()
                                                     : "UNKNOWN") + ")");
            w.end();
    }

    public String toString() {
          // This is really slow and so you are encouraged to override.
          // return new StringPrettyPrinter(5).toString(this);

          // Not slow anymore.
          return "<unknown-node>";
    }
}
