package jltools.ast;

import jltools.util.CodeWriter;
import jltools.util.Position;
import jltools.util.Copy;
import jltools.types.Context;
import jltools.types.SemanticException;
import jltools.types.TypeSystem;
import jltools.visit.NodeVisitor;
import jltools.visit.TypeBuilder;
import jltools.visit.AmbiguityRemover;
import jltools.visit.AddMemberVisitor;
import jltools.visit.ConstantFolder;
import jltools.visit.TypeChecker;
import jltools.visit.ExpectedTypeVisitor;
import jltools.visit.ExceptionChecker;
import jltools.visit.Translator;

import java.io.Serializable;

/**
 * A <code>Node</code> represents an AST node.  All AST nodes must implement
 * this interface.  Nodes should be immutable: methods which set fields
 * of the node should copy the node, set the field in the copy, and then
 * return the copy.
 */
public interface Node extends Copy, Serializable
{
    /**
     * Return true if the node should be bypassed on the next visit.
     */
    boolean bypass();

    /**
     * Create a new node with the bypass flag set to <code>bypass</code>.
     */
    Node bypass(boolean bypass);

    /**
     * Create a new node with the bypass flag set to true for all children
     * of the node.
     */
    Node bypassChildren();

    /**
     * Return the delegate for this node.  Some operations on the node should
     * be invoked only through the delegate, for instance as:
     * <pre>
     *    n.ext().typeCheck(c)
     * </pre>
     * rather than:
     * <pre>
     *    n.typeCheck_(c)
     * </pre>
     */
    Ext ext();

    /** Create a copy of the node with a new delegate. */
    Node ext(Ext ext);

    /** Get the position of the node in the source file.  Returns null if
     * the position is not set. */
    Position position();

    /** Create a copy of the node with a new position. */
    Node position(Position position);

    /** Clone the node. */
    Object copy();

    /**
     * Visit the node.  This method is equivalent to <code>visitEdge(null,
     * v)</code>.
     *
     * @param v The visitor which will traverse/rewrite the AST.
     * @return A new AST if a change was made, or <code>this</code>.
     */
    Node visit(NodeVisitor v);

    /**
     * Visit the node, passing in the node's parent.  This method is called by
     * a <code>NodeVisitor</code> to traverse the AST starting at this node.
     * This method should call the <code>override</code>, <code>enter</code>,
     * and <code>leave<code> methods of the visitor.  The method may return a
     * new version of the node.
     *
     * @param parent The parent of <code>this</code> in the AST.
     * @param v The visitor which will traverse/rewrite the AST.
     * @return A new AST if a change was made, or <code>this</code>.
     */
    Node visitEdge(Node parent, NodeVisitor v);

    /**
     * Visit the children of the node.
     *
     * @param v The visitor which will traverse/rewrite the AST.
     * @return A new AST if a change was made, or <code>this</code>.
     */
    Node visitChildren(NodeVisitor v);

    /**
     * Visit a single child of the node.
     *
     * @param v The visitor which will traverse/rewrite the AST.
     * @return The result of <code>child.visit(v)</code>, or <code>null</code>
     * if <code>child</code> was <code>null</code>.
     */
    Node visitChild(Node child, NodeVisitor v);

    /**
     * Adjust the environment on entering the scope of the method.
     */
    void enterScope(Context c);

    /**
     * Adjust the environment on leaving the scope of the method.
     */
    void leaveScope(Context c);

    // Implementations of the default passes.  These methods should only
    // be called through the delegate.

    /**
     * Collects classes, methods, and fields from the AST rooted at this node
     * and constructs type objects for these.  These type objects may be
     * ambiguous.
     *
     * @param cb The visitor which adds new type objects to the
     * <code>TypeSystem</code>.
     */
    Node buildTypesOverride_(TypeBuilder tb) throws SemanticException;
    Node buildTypesEnter_(TypeBuilder tb) throws SemanticException;
    Node buildTypes_(TypeBuilder tb) throws SemanticException;

    /**
     * Adds disambiguated methods and fields to the types.
     *
     * @param tc The visitor which builds types.
     */
    Node addMembersOverride_(AddMemberVisitor tc) throws SemanticException;
    Node addMembersEnter_(AddMemberVisitor tc) throws SemanticException;
    Node addMembers_(AddMemberVisitor tc) throws SemanticException;

    /**
     * Remove any remaining ambiguities from the AST.
     *
     * @param ar The visitor which disambiguates.
     */
    Node disambiguateOverride_(AmbiguityRemover ar) throws SemanticException;
    Node disambiguateEnter_(AmbiguityRemover ar) throws SemanticException;
    Node disambiguate_(AmbiguityRemover ar) throws SemanticException;

    /**
     * Fold constants in the AST.
     *
     * @param cf The constant folding visitor.
     */
    Node foldConstantsOverride_(ConstantFolder cf);
    Node foldConstantsEnter_(ConstantFolder cf);
    Node foldConstants_(ConstantFolder cf);

    /**
     * Type check the AST.
     *
     * @param tc The type checking visitor.
     */
    Node typeCheckOverride_(TypeChecker tc) throws SemanticException;
    Node typeCheckEnter_(TypeChecker tc) throws SemanticException;
    Node typeCheck_(TypeChecker tc) throws SemanticException;
    Expr setExpectedType_(Expr child, ExpectedTypeVisitor tc) throws SemanticException;

    /**
     * Check that exceptions are properly propagated throughout the AST.
     *
     * @param ec The visitor.
     */
    Node exceptionCheckOverride_(ExceptionChecker ec) throws SemanticException;
    Node exceptionCheckEnter_(ExceptionChecker ec) throws SemanticException;
    Node exceptionCheck_(ExceptionChecker ec) throws SemanticException;

    /**
     * Translate the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param tr The translation pass.  This is <i>not</i> a visitor.
     */
    void translate_(CodeWriter w, Translator tr);

    /**
     * Dump the AST node for debugging purposes.
     */
    void dump(CodeWriter w);
}
