package polyglot.ast;

import polyglot.util.Copy;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.Type;
import polyglot.visit.*;

/**
 * A <code>Node</code> represents an AST node.  All AST nodes must implement
 * this interface.  Nodes should be immutable: methods which set fields
 * of the node should copy the node, set the field in the copy, and then
 * return the copy.
 */
public interface Node extends NodeOps, Copy
{
    /**
     * Set the delegate of the node.
     */
    Node del(Del del);

    /**
     * Get the node's delegate.
     */
    Del del();

    /**
     * Get the position of the node in the source file.  Returns null if
     * the position is not set.
     */
    Position position();

    /** Create a copy of the node with a new position. */
    Node position(Position position);

    /**
     * Visit the node.  This method is equivalent to
     * <code>visitEdge(null, v)</code>.
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
     * @param child The child to visit.
     * @return The result of <code>child.visit(v)</code>, or <code>null</code>
     * if <code>child</code> was <code>null</code>.
     */
    Node visitChild(Node child, NodeVisitor v);

    /**
     * Adjust the environment on entering the scope of the method.
     */
    Context enterScope(Context c);

    /**
     * Adjust the environment on leaving the scope of the method.
     */
    // void leaveScope(Context c);

    public Type childExpectedType(Expr child, AscriptionVisitor av);

    /**
     * Dump the AST node for debugging purposes.
     */
    void dump(CodeWriter w);

    //////////////////////////////////////////////////////////////// 
    // Duplicate the NodeOps interface, but deprecate the methods.
    // That way, we'll get a warning if we try to call these directly.
    //////////////////////////////////////////////////////////////// 

    /** @deprectated */
    public Node buildTypesOverride(TypeBuilder tb) throws SemanticException;
    /** @deprectated */
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException;
    /** @deprectated */
    public Node buildTypes(TypeBuilder tb) throws SemanticException;
    /** @deprectated */
    public Node disambiguateOverride(AmbiguityRemover ar) throws SemanticException;
    /** @deprectated */
    public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException;
    /** @deprectated */
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException;
    /** @deprectated */
    public Node addMembersOverride(AddMemberVisitor am) throws SemanticException;
    /** @deprectated */
    public NodeVisitor addMembersEnter(AddMemberVisitor am) throws SemanticException;
    /** @deprectated */
    public Node addMembers(AddMemberVisitor am) throws SemanticException;
    /** @deprectated */
    public Node foldConstantsOverride(ConstantFolder cf);
    /** @deprectated */
    public NodeVisitor foldConstantsEnter(ConstantFolder cf);
    /** @deprectated */
    public Node foldConstants(ConstantFolder cf);
    /** @deprectated */
    public Node typeCheckOverride(TypeChecker tc) throws SemanticException;
    /** @deprectated */
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException;
    /** @deprectated */
    public Node typeCheck(TypeChecker tc) throws SemanticException;
    /** @deprectated */
    public Node exceptionCheckOverride(ExceptionChecker ec) throws SemanticException;
    /** @deprectated */
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec) throws SemanticException;
    /** @deprectated */
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException;
    /** @deprectated */
    public void translate(CodeWriter w, Translator tr);
    /** @deprectated */
    public void prettyPrint(CodeWriter w, PrettyPrinter pp);
}
