package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 * It defines default implementations of the methods which implement compiler
 * passes, dispatching to the node to perform the actual work of the pass.
 */
public class Ext_c implements Ext {
    /**
     * The object we are extending.  <code>basis</code> will be
     * <code>this</code> if basis is a <code>Node</code>.
     */
    protected Ext basis;

    /**
     * Our extension object.  <code>ext</code> will be
     * <code>this</code> if we are not extended.
     */
    protected Ext ext;

    public Ext_c() {
        this.ext = this;
        this.basis = this;
    }

    public Ext_c(Ext ext) {
        this();

        if (ext != null) {
            this.ext = ext;
            ext.setBasis(this);
        }
    }

    public void setBasis(Ext basis) {
	this.basis = basis;
    }

    public Ext basis() {
        return basis;
    }

    public Node node() {
	return basis.node();
    }

    public Ext ext() {
        return ext;
    }

    public Object copy() {
	try {
            Ext_c copy = (Ext_c) super.clone();

            // Be careful to preserve the topology of the "ext" and "basis"
            // pointers.

            if (this.ext == this) {
                copy.ext = copy;
            }
            else {
                copy.ext = ext;
            }

            copy.ext.setBasis(copy);

            return copy;
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalCompilerError("Java clone() wierdness.");
	}
    }

    public Ext setExt(Ext ext) {
        if (this.ext == ext) {
            return this;
        }

        try {
            Ext_c x = (Ext_c) super.clone();
            x.ext = ext;
            ext.setBasis(x);
            return x;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() wierdness.");
        }
    }

    // By default, delegate the functionality to the basis node.
    public Node buildTypesOverride_(TypeBuilder tb) throws SemanticException {
        cycleCheck();
	return basis.buildTypesOverride_(tb);
    }

    public Node buildTypesEnter_(TypeBuilder tb) throws SemanticException {
        cycleCheck();
	return basis.buildTypesEnter_(tb);
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
        cycleCheck();
	return basis.buildTypes_(tb);
    }

    public Node addMembersOverride_(AddMemberVisitor am) throws SemanticException {
        cycleCheck();
	return basis.addMembersOverride_(am);
    }

    public Node addMembersEnter_(AddMemberVisitor am) throws SemanticException {
        cycleCheck();
	return basis.addMembersEnter_(am);
    }

    public Node addMembers_(AddMemberVisitor am) throws SemanticException {
        cycleCheck();
	return basis.addMembers_(am);
    }

    public Node disambiguateOverride_(AmbiguityRemover ar) throws SemanticException {
        cycleCheck();
	return basis.disambiguateOverride_(ar);
    }

    public Node disambiguateEnter_(AmbiguityRemover ar) throws SemanticException {
        cycleCheck();
	return basis.disambiguateEnter_(ar);
    }

    public Node disambiguate_(AmbiguityRemover ar) throws SemanticException {
        cycleCheck();
	return basis.disambiguate_(ar);
    }

    public Node foldConstantsOverride_(ConstantFolder cf) {
        cycleCheck();
	return basis.foldConstantsOverride_(cf);
    }

    public Node foldConstantsEnter_(ConstantFolder cf) {
        cycleCheck();
	return basis.foldConstantsEnter_(cf);
    }

    public Node foldConstants_(ConstantFolder cf) {
        cycleCheck();
	return basis.foldConstants_(cf);
    }

    public Node typeCheckOverride_(TypeChecker tc) throws SemanticException {
        cycleCheck();
	return basis.typeCheckOverride_(tc);
    }

    public Node typeCheckEnter_(TypeChecker tc) throws SemanticException {
        cycleCheck();
	return basis.typeCheckEnter_(tc);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        cycleCheck();
	return basis.typeCheck_(tc);
    }

    public Expr setExpectedType_(Expr child, ExpectedTypeVisitor tc) throws SemanticException {
        cycleCheck();
        return basis.setExpectedType_(child, tc);
    }

    public Node exceptionCheckOverride_(ExceptionChecker ec) throws SemanticException {
        cycleCheck();
	return basis.exceptionCheckOverride_(ec);
    }

    public Node exceptionCheckEnter_(ExceptionChecker ec) throws SemanticException {
        cycleCheck();
	return basis.exceptionCheckEnter_(ec);
    }

    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
        cycleCheck();
	return basis.exceptionCheck_(ec);
    }

    public void translate_(CodeWriter w, Translator tr) {
        cycleCheck();
	basis.translate_(w, tr);
    }

    private void cycleCheck() {
        if (basis == this) {
            throw new InternalCompilerError("Must override this method in " +
                                            basis.getClass().getName() +
                                            " or a subclass.");
        }
    }

    /**
    * Collects classes, methods, and fields from the AST rooted at this node
    * and constructs type objects for these.  These type objects may be
    * ambiguous.
    *
    * @param cb The visitor which adds new type objects to the
    * <code>TypeSystem</code>.
    */
    public Node buildTypesOverride(TypeBuilder tb) throws SemanticException {
        if (ext != this) {
            return ext.buildTypesOverride(tb);
        }
        else {
            return this.buildTypesOverride_(tb);
        }
    }

    public Node buildTypesEnter(TypeBuilder tb) throws SemanticException {
        if (ext != this) {
            return ext.buildTypesEnter(tb);
        }
        else {
            return this.buildTypesEnter_(tb);
        }
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        if (ext != this) {
            return ext.buildTypes(tb);
        }
        else {
            return this.buildTypes_(tb);
        }
    }


    /**
    * Adds disambiguated methods and fields to the types.
    *
    * @param am The visitor which builds types.
    */
    public Node addMembersOverride(AddMemberVisitor am) throws SemanticException {
        if (ext != this) {
            return ext.addMembersOverride(am);
        }
        else {
            return this.addMembersOverride_(am);
        }
    }

    public Node addMembersEnter(AddMemberVisitor am) throws SemanticException {
        if (ext != this) {
            return ext.addMembersEnter(am);
        }
        else {
            return this.addMembersEnter_(am);
        }
    }

    public Node addMembers(AddMemberVisitor am) throws SemanticException {
        if (ext != this) {
            return ext.addMembers(am);
        }
        else {
            return this.addMembers_(am);
        }
    }

    /**
    * Remove any remaining ambiguities from the AST.
    *
    * @param ar The visitor which disambiguates.
    */
    public Node disambiguateOverride(AmbiguityRemover ar) throws SemanticException {
        if (ext != this) {
            return ext.disambiguateOverride(ar);
        }
        else {
            return this.disambiguateOverride_(ar);
        }
    }

    public Node disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
        if (ext != this) {
            return ext.disambiguateEnter(ar);
        }
        else {
            return this.disambiguateEnter_(ar);
        }
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (ext != this) {
            return ext.disambiguate(ar);
        }
        else {
            return this.disambiguate_(ar);
        }
    }


    /**
    * Fold constants in the AST.
    *
    * @param cf The constant folding visitor.
    */
    public Node foldConstantsOverride(ConstantFolder cf) {
        if (ext != this) {
            return ext.foldConstantsOverride(cf);
        }
        else {
            return this.foldConstantsOverride_(cf);
        }
    }

    public Node foldConstantsEnter(ConstantFolder cf) {
        if (ext != this) {
            return ext.foldConstantsEnter(cf);
        }
        else {
            return this.foldConstantsEnter_(cf);
        }
    }

    public Node foldConstants(ConstantFolder cf) {
        if (ext != this) {
            return ext.foldConstants(cf);
        }
        else {
            return this.foldConstants_(cf);
        }
    }


    /**
    * Type check the AST.
    *
    * @param tc The type checking visitor.
    */
    public Node typeCheckOverride(TypeChecker tc) throws SemanticException {
        if (ext != this) {
            return ext.typeCheckOverride(tc);
        }
        else {
            return this.typeCheckOverride_(tc);
        }
    }

    public Node typeCheckEnter(TypeChecker tc) throws SemanticException {
        if (ext != this) {
            return ext.typeCheckEnter(tc);
        }
        else {
            return this.typeCheckEnter_(tc);
        }
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (ext != this) {
            return ext.typeCheck(tc);
        }
        else {
            return this.typeCheck_(tc);
        }
    }

    /**
    * Set the expected type of <code>child</code>.  This method is called
    * by the visitor just before the child expression is visited.
    *
    * @param child An immediate subexpression of <code>this</code>.
    * @param tc The expected type visitor.
    * @return A new version of child with the expected type field set.
    */
    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
        throws SemanticException {
        if (ext != this) {
            return ext.setExpectedType(child, tc);
        }
        else {
            return this.setExpectedType_(child, tc);
        }
    }


    /**
    * Check that exceptions are properly propagated throughout the AST.
    *
    * @param ec The visitor.
    */
    public Node exceptionCheckOverride(ExceptionChecker ec) throws SemanticException {
        if (ext != this) {
            return ext.exceptionCheckOverride(ec);
        }
        else {
            return this.exceptionCheckOverride_(ec);
        }
    }

    public Node exceptionCheckEnter(ExceptionChecker ec) throws SemanticException {
        if (ext != this) {
            return ext.exceptionCheckEnter(ec);
        }
        else {
            return this.exceptionCheckEnter_(ec);
        }
    }

    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        if (ext != this) {
            return ext.exceptionCheck(ec);
        }
        else {
            return this.exceptionCheck_(ec);
        }
    }

    /**
    * Translate the AST using the given code writer.
    *
    * @param w The code writer to which to write.
    * @param tr The translation pass.  This is <i>not</i> a visitor.
    */
    public void translate(CodeWriter w, Translator tr) {
        if (ext != this) {
            ext.translate(w, tr);
        }
        else {
            this.translate_(w, tr);
        }
    }

    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName());
    }
}
