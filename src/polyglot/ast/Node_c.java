package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

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
	protected Ext ext;
	protected Position position;
        protected boolean bypass;

	public Node_c(Ext ext, Position pos) {
		this.ext = ext;
		ext.init(this);
		this.position = pos;
                this.bypass = false;
	}

	public Object copy() {
		return ext((Ext) ext.copy());
	}

	public Ext ext() {
		return this.ext;
	}

	public Node ext(Ext ext) {
		try {
			Node_c n = (Node_c) super.clone();
			n.ext = ext;
			ext.init(n);
			return n;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalCompilerError("Java clone() wierdness.");
		}
	}

        public boolean bypass() {
                return bypass;
        }

        public Node bypass(boolean bypass) {
                if (this.bypass != bypass) {
                    Node_c n = (Node_c) copy();
                    n.bypass = bypass;
                    return n;
                }

                return this;
        }

        static class BypassChildrenVisitor extends NodeVisitor {
            public Node override(Node n) {
                return n.bypass(true);
            }
        }

        static BypassChildrenVisitor bcv = new BypassChildrenVisitor();

        public Node bypassChildren() {
                return bypass(false).visitChildren(bcv);
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

                // return v.visitEdge(this, n);
                return n.visit(v);
        }

        public Node visit(NodeVisitor v) {
                if (bypass) {
                    Types.report(5, "skipping " + this);
                    return bypass(false);
                }

		Node n = v.override(this);

		if (n == null) {
			n = v.enter(this);

			if (n == null) {
				throw new InternalCompilerError(
					"NodeVisitor.enter() returned null.");
			}

			n = n.visitChildren(v);

			if (n == null) {
				throw new InternalCompilerError(
					"Node_c.visitChildren() returned null.");
			}

			n = v.leave(this, n, v);

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
	 *	       and also typed the same as <code>l</code>, if <code>l</code>
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
	public void enterScope(Context c) { }

	/** Adjust the environment for leaving the current scope. */
	public void leaveScope(Context c) { }

	public Node buildTypesOverride_(TypeBuilder tb) throws SemanticException {
		return null;
	}

	public Node buildTypesEnter_(TypeBuilder tb) throws SemanticException {
                return this;
	}

	public Node buildTypes_(TypeBuilder tb) throws SemanticException {
		return this;
	}

	/** Remove any remaining ambiguities from the AST. */
	public Node disambiguateOverride_(AmbiguityRemover ar) throws SemanticException {
		return null;
	}

	public Node disambiguateEnter_(AmbiguityRemover ar) throws SemanticException {
                return this;
	}

	public Node disambiguate_(AmbiguityRemover ar) throws SemanticException {
		return this;
	}

	/** Add members to a class. */
	public Node addMembersOverride_(AddMemberVisitor am) throws SemanticException {
		return null;
	}

	public Node addMembersEnter_(AddMemberVisitor am) throws SemanticException {
                return this;
	}

	public Node addMembers_(AddMemberVisitor am) throws SemanticException {
		return this;
	}

	/** Fold all constants. */
	public Node foldConstantsOverride_(ConstantFolder cf) {
		return null;
	}

	public Node foldConstantsEnter_(ConstantFolder cf) {
                return this;
	}

	public Node foldConstants_(ConstantFolder cf) {
		return this;
	}

	/** Type check the AST. */
	public Node typeCheckOverride_(TypeChecker tc) throws SemanticException {
		return null;
	}

	public Node typeCheckEnter_(TypeChecker tc) throws SemanticException {
                return this;
	}

	public Node typeCheck_(TypeChecker tc) throws SemanticException {
		return this;
	}

	/** Check that exceptions are properly propagated throughout the AST. */
	public Node exceptionCheckOverride_(ExceptionChecker ec) throws SemanticException {
		return null;
	}

	public Node exceptionCheckEnter_(ExceptionChecker ec) throws SemanticException {
                return this;
	}

	public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
		return this;
	}

	/** Translate the AST using the given <code>CodeWriter</code>. */
	public void translate_(CodeWriter w, Translator tr) { }

	public void translateBlock(Node n, CodeWriter w, Translator tr) {
		w.begin(0);
		n.ext().translate(w, tr);
		w.end();
	}

	public void translateSubstmt(Stmt stmt, CodeWriter w, Translator tr) {
		w.allowBreak(4, " ");
		translateBlock(stmt, w, tr);
	}

	public void dump(CodeWriter w) {
		w.write(StringUtil.getShortNameComponent(getClass().getName()));

		w.allowBreak(4, " ");
		w.begin(0);
		w.write("(ext " + ext() + ")");
		w.end();

		w.allowBreak(4, " ");
		w.begin(0);
		w.write("(position " + (position != null
								? position.toString()
								  : "UNKNOWN") + ")");
		w.end();

		w.allowBreak(4, " ");
		w.begin(0);
		w.write("(bypass " + bypass() + ")");
		w.end();
	}

	public class StringCodeWriter extends CodeWriter {
		CharArrayWriter w;

		public StringCodeWriter(CharArrayWriter w) {
			super(w, 1000);
			this.w = w;
		}

		public void newline(int n) { }
		public void allowBreak(int n) { super.write(" "); }
		public void allowBreak(int n, String alt) { super.write(alt); }

		public String toString() {
			return w.toString();
		}
	}

	public String toString() {
		StringCodeWriter w = new StringCodeWriter(new CharArrayWriter());

		DumpAst v = new DumpAst(w);
		bypass(false).visit(v);
		v.finish();

		return w.toString();
	}
}
