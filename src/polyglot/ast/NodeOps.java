package polyglot.ast;

import polyglot.util.CodeWriter;
import polyglot.types.SemanticException;
import polyglot.visit.*;

/**
 * A <code>Node</code> represents an AST node.  All AST nodes must implement
 * this interface.  Nodes should be immutable: methods which set fields
 * of the node should copy the node, set the field in the copy, and then
 * return the copy.
 */
public interface NodeOps
{
    /*
     * FIXME: insert this comment before every Override method.
     *
     * @deprecated Don't use this method.  Try to use the methods
     * called by <code>enter()</code> and <code>leave()</code>.
     */

    /**
     * Collects classes, methods, and fields from the AST rooted at this node
     * and constructs type objects for these.  These type objects may be
     * ambiguous.  Inserts classes into the <code>TypeSystem</code>.
     *
     * This method is called by the <code>override()</code> method of
     * the visitor.  This method should perform the work for the node
     * and for its children, possibly be directly invoking
     * <code>this.visitChildren()</code> to have the visitor visit the
     * children.
     *
     * The method should either return <code>null</code>--in which case
     * the visitor will call the <code>enter()</code> method,
     * <code>visitChildren()</code>, and <code>leave()</code>--or the
     * method should explicitly visit the children and return a new
     * node.
     *
     * @param tb The visitor which adds new type objects to the
     * <code>TypeSystem</code>.
     */
    Node buildTypesOverride(TypeBuilder tb) throws SemanticException;

    /**
     * Collects classes, methods, and fields from the AST rooted at this node
     * and constructs type objects for these.  These type objects may be
     * ambiguous.  Inserts classes into the <code>TypeSystem</code>.
     *
     * This method is called by the <code>enter()</code> method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node on which
     * <code>visitChildren()</code> and <code>leave()</code> will be
     * invoked.
     *
     * @param tb The visitor which adds new type objects to the
     * <code>TypeSystem</code>.
     */
    NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException;

    /**
     * Collects classes, methods, and fields from the AST rooted at this node
     * and constructs type objects for these.  These type objects may be
     * ambiguous.  Inserts classes into the <code>TypeSystem</code>.
     *
     * This method is called by the <code>leave()</code> method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param tb The visitor which adds new type objects to the
     * <code>TypeSystem</code>.
     */
    Node buildTypes(TypeBuilder tb) throws SemanticException;

    /**
     * Remove any remaining ambiguities from the AST.
     *
     * This method is called by the <code>override()</code> method of
     * the visitor.  This method should perform the work for the node
     * and for its children, possibly be directly invoking
     * <code>this.visitChildren()</code> to have the visitor visit the
     * children.
     *
     * The method should either return <code>null</code>--in which case
     * the visitor will call the <code>enter()</code> method,
     * <code>visitChildren()</code>, and <code>leave()</code>--or the
     * method should explicitly visit the children and return a new
     * node.
     *
     * @param ar The visitor which disambiguates.
     */
    Node disambiguateOverride(AmbiguityRemover ar) throws SemanticException;

    /**
     * Remove any remaining ambiguities from the AST.
     *
     * This method is called by the <code>enter()</code> method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node on which
     * <code>visitChildren()</code> and <code>leave()</code> will be
     * invoked.
     *
     * @param ar The visitor which disambiguates.
     */
    NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException;

    /**
     * Remove any remaining ambiguities from the AST.
     *
     * This method is called by the <code>leave()</code> method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param ar The visitor which disambiguates.
     */
    Node disambiguate(AmbiguityRemover ar) throws SemanticException;

    /**
     * Adds disambiguated methods and fields to the types.
     *
     * This method is called by the <code>override()</code> method of
     * the visitor.  This method should perform the work for the node
     * and for its children, possibly be directly invoking
     * <code>this.visitChildren()</code> to have the visitor visit the
     * children.
     *
     * The method should either return <code>null</code>--in which case
     * the visitor will call the <code>enter()</code> method,
     * <code>visitChildren()</code>, and <code>leave()</code>--or the
     * method should explicitly visit the children and return a new
     * node.
     *
     * @param am The visitor which builds types.
     */
    Node addMembersOverride(AddMemberVisitor am) throws SemanticException;

    /**
     * Adds disambiguated methods and fields to the types.
     *
     * This method is called by the <code>enter()</code> method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node on which
     * <code>visitChildren()</code> and <code>leave()</code> will be
     * invoked.
     *
     * @param am The visitor which builds types.
     */
    NodeVisitor addMembersEnter(AddMemberVisitor am) throws SemanticException;

    /**
     * Adds disambiguated methods and fields to the types.
     *
     * This method is called by the <code>leave()</code> method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param am The visitor which builds types.
     */
    Node addMembers(AddMemberVisitor am) throws SemanticException;

    /**
     * Fold constants in the AST.
     *
     * This method is called by the <code>override()</code> method of
     * the visitor.  This method should perform the work for the node
     * and for its children, possibly be directly invoking
     * <code>this.visitChildren()</code> to have the visitor visit the
     * children.
     *
     * The method should either return <code>null</code>--in which case
     * the visitor will call the <code>enter()</code> method,
     * <code>visitChildren()</code>, and <code>leave()</code>--or the
     * method should explicitly visit the children and return a new
     * node.
     *
     * @param cf The constant folding visitor.
     */
    Node foldConstantsOverride(ConstantFolder cf);

    /**
     * Fold constants in the AST.
     *
     * This method is called by the <code>enter()</code> method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node on which
     * <code>visitChildren()</code> and <code>leave()</code> will be
     * invoked.
     *
     * @param cf The constant folding visitor.
     */
    NodeVisitor foldConstantsEnter(ConstantFolder cf);

    /**
     * Fold constants in the AST.
     *
     * This method is called by the <code>leave()</code> method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param cf The constant folding visitor.
     */
    Node foldConstants(ConstantFolder cf);

    /**
     * Type check the AST.
     *
     * This method is called by the <code>override()</code> method of
     * the visitor.  This method should perform the work for the node
     * and for its children, possibly be directly invoking
     * <code>this.visitChildren()</code> to have the visitor visit the
     * children.
     *
     * The method should either return <code>null</code>--in which case
     * the visitor will call the <code>enter()</code> method,
     * <code>visitChildren()</code>, and <code>leave()</code>--or the
     * method should explicitly visit the children and return a new
     * node.
     *
     * @param tc The type checking visitor.
     */
    Node typeCheckOverride(TypeChecker tc) throws SemanticException;

    /**
     * Type check the AST.
     *
     * This method is called by the <code>enter()</code> method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node on which
     * <code>visitChildren()</code> and <code>leave()</code> will be
     * invoked.
     *
     * @param tc The type checking visitor.
     */
    NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException;

    /**
     * Type check the AST.
     *
     * This method is called by the <code>leave()</code> method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param tc The type checking visitor.
     */
    Node typeCheck(TypeChecker tc) throws SemanticException;

    /**
     * Set the expected type of <code>child</code>.  This method is called
     * by the visitor just before the child expression is visited.
     *
     * @param child An immediate subexpression of <code>node()</code>.
     * @param tv The expected type visitor.
     * @return A new version of child with the expectedType() field set.
     */
    Expr setExpectedType(Expr child, ExpectedTypeVisitor tv)
        throws SemanticException;

    /**
     * Check that exceptions are properly propagated throughout the AST.
     *
     * This method is called by the <code>override()</code> method of
     * the visitor.  This method should perform the work for the node
     * and for its children, possibly be directly invoking
     * <code>this.visitChildren()</code> to have the visitor visit the
     * children.
     *
     * The method should either return <code>null</code>--in which case
     * the visitor will call the <code>enter()</code> method,
     * <code>visitChildren()</code>, and <code>leave()</code>--or the
     * method should explicitly visit the children and return a new
     * node.
     *
     * @param ec The visitor.
     */
    Node exceptionCheckOverride(ExceptionChecker ec) throws SemanticException;

    /**
     * Check that exceptions are properly propagated throughout the AST.
     *
     * This method is called by the <code>enter()</code> method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node on which
     * <code>visitChildren()</code> and <code>leave()</code> will be
     * invoked.
     *
     * @param ec The visitor.
     */
    NodeVisitor exceptionCheckEnter(ExceptionChecker ec) throws SemanticException;

    /**
     * Check that exceptions are properly propagated throughout the AST.
     *
     * This method is called by the <code>leave()</code> method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param ec The visitor.
     */
    Node exceptionCheck(ExceptionChecker ec) throws SemanticException;

    /**
     * Pretty-print the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param pp The pretty printer.  This is <i>not</i> a visitor.
     */
    void prettyPrint(CodeWriter w, PrettyPrinter pp);

    /**
     * Translate the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param tr The translation pass.  This is <i>not</i> a visitor.
     */
    void translate(CodeWriter w, Translator tr);
}
