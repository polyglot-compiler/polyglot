package jltools.ast;

import jltools.util.CodeWriter;
import jltools.util.Copy;
import jltools.visit.TypeBuilder;
import jltools.visit.AmbiguityRemover;
import jltools.visit.ConstantFolder;
import jltools.visit.AddMemberVisitor;
import jltools.visit.TypeChecker;
import jltools.visit.ExpectedTypeVisitor;
import jltools.visit.ExceptionChecker;
import jltools.visit.Translator;
import jltools.types.SemanticException;
import jltools.types.TypeSystem;
import jltools.types.Context;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 * It declares the methods which implement compiler passes.
 */
public interface Ext extends Copy
{
    /** Copy the Ext. */
    Object copy();

    /** Initialize the extension with the given base object. */
    void setBasis(Ext basis);

    /**
     * Return the delegate for this node.  Operations on this Ext may be
     * delegated to ext().
     */
    Ext ext();

    /** Set the ext field. */
    Ext setExt(Ext ext);

    /**
     * The object we are immediately extending, which may in turn be extending
     * another object.  (I would have called it base(), but that was already
     * taken by a subtype method).
     */
    Ext basis();

    /**
     * The node with a ultimately extending.  The following invariant should
     * hold:
     * <pre>
     *    this.node() == basis().node() == node().node()
     * </pre>
     */
    Node node();

    Node buildTypesOverride_(TypeBuilder tb) throws SemanticException;
    Node buildTypesEnter_(TypeBuilder tb) throws SemanticException;
    Node buildTypes_(TypeBuilder tb) throws SemanticException;

    Node addMembersOverride_(AddMemberVisitor tc) throws SemanticException;
    Node addMembersEnter_(AddMemberVisitor tc) throws SemanticException;
    Node addMembers_(AddMemberVisitor tc) throws SemanticException;

    Node disambiguateOverride_(AmbiguityRemover ar) throws SemanticException;
    Node disambiguateEnter_(AmbiguityRemover ar) throws SemanticException;
    Node disambiguate_(AmbiguityRemover ar) throws SemanticException;

    Node foldConstantsOverride_(ConstantFolder cf);
    Node foldConstantsEnter_(ConstantFolder cf);
    Node foldConstants_(ConstantFolder cf);

    Node typeCheckOverride_(TypeChecker tc) throws SemanticException;
    Node typeCheckEnter_(TypeChecker tc) throws SemanticException;
    Node typeCheck_(TypeChecker tc) throws SemanticException;
    Expr setExpectedType_(Expr child, ExpectedTypeVisitor tc) throws SemanticException;

    Node exceptionCheckOverride_(ExceptionChecker ec) throws SemanticException;
    Node exceptionCheckEnter_(ExceptionChecker ec) throws SemanticException;
    Node exceptionCheck_(ExceptionChecker ec) throws SemanticException;

    void translate_(CodeWriter w, Translator tr);

    /**
     * Collects classes, methods, and fields from the AST rooted at this node
     * and constructs type objects for these.  These type objects may be
     * ambiguous.
     *
     * @param cb The visitor which adds new type objects to the
     * <code>TypeSystem</code>.
     */
    Node buildTypesOverride(TypeBuilder tb) throws SemanticException;
    Node buildTypesEnter(TypeBuilder tb) throws SemanticException;
    Node buildTypes(TypeBuilder tb) throws SemanticException;

    /**
     * Adds disambiguated methods and fields to the types.
     *
     * @param tc The visitor which builds types.
     */
    Node addMembersOverride(AddMemberVisitor tc) throws SemanticException;
    Node addMembersEnter(AddMemberVisitor tc) throws SemanticException;
    Node addMembers(AddMemberVisitor tc) throws SemanticException;

    /**
     * Remove any remaining ambiguities from the AST.
     *
     * @param ar The visitor which disambiguates.
     */
    Node disambiguateOverride(AmbiguityRemover ar) throws SemanticException;
    Node disambiguateEnter(AmbiguityRemover ar) throws SemanticException;
    Node disambiguate(AmbiguityRemover ar) throws SemanticException;

    /**
     * Fold constants in the AST.
     *
     * @param cf The constant folding visitor.
     */
    Node foldConstantsOverride(ConstantFolder cf);
    Node foldConstantsEnter(ConstantFolder cf);
    Node foldConstants(ConstantFolder cf);

    /**
     * Type check the AST.
     *
     * @param tc The type checking visitor.
     */
    Node typeCheckOverride(TypeChecker tc) throws SemanticException;
    Node typeCheckEnter(TypeChecker tc) throws SemanticException;
    Node typeCheck(TypeChecker tc) throws SemanticException;

    /**
     * Set the expected type of <code>child</code>.  This method is called
     * by the visitor just before the child expression is visited.
     *
     * @param child An immediate subexpression of <code>this</code>.
     * @param tc The expected type visitor.
     * @return A new version of child with the expected type field set.
     */
    Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
        throws SemanticException;

    /**
     * Check that exceptions are properly propagated throughout the AST.
     *
     * @param ec The visitor.
     */
    Node exceptionCheckOverride(ExceptionChecker ec) throws SemanticException;
    Node exceptionCheckEnter(ExceptionChecker ec) throws SemanticException;
    Node exceptionCheck(ExceptionChecker ec) throws SemanticException;

    /**
     * Translate the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param tr The translation pass.  This is <i>not</i> a visitor.
     */
    void translate(CodeWriter w, Translator tr);
}
