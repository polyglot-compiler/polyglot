package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * <code>JL</code> is the super type of all node extension objects.
 * It defines default implementations of the methods which implement compiler
 * passes, dispatching to the node to perform the actual work of the pass.
 */
public class JL_c extends Ext_c implements JL {
    public JL_c() {
    }

    public JL jl() {
        return node();
    }

    /**
     * Visit the children of the node.
     *
     * @param v The visitor that will traverse/rewrite the AST.
     * @return A new AST if a change was made, or <code>this</code>.
     */
    public Node visitChildren(NodeVisitor v) {
        return jl().visitChildren(v);
    }

    /**
     * Push a new scope for visiting children and add any declarations
     * to the new context that should be in scope when visiting
     * children.  This should <i>not</i> update the old context
     * imperatively.  Use <code>addDecls</code> when leaving the node
     * for that.
     *
     * @param c The context in which to enter scope.
     */
    public Context enterScope(Context c) {
        return jl().enterScope(c);
    }

    /**
     * Add any declarations to the context that should be in scope when
     * visiting later sibling nodes.
     *
     * @param c The context to which to add declarations.
     */
    public void addDecls(Context c) {
        jl().addDecls(c);
    }

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
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
	return jl().buildTypesEnter(tb);
    }

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
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
	return jl().buildTypes(tb);
    }

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
    public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
	return jl().disambiguateEnter(ar);
    }

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
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	return jl().disambiguate(ar);
    }

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
    public NodeVisitor addMembersEnter(AddMemberVisitor am) throws SemanticException {
	return jl().addMembersEnter(am);
    }

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
    public Node addMembers(AddMemberVisitor am) throws SemanticException {
	return jl().addMembers(am);
    }

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
    public NodeVisitor foldConstantsEnter(ConstantFolder cf) {
	return jl().foldConstantsEnter(cf);
    }

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
    public Node foldConstants(ConstantFolder cf) {
	return jl().foldConstants(cf);
    }

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
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
	return jl().typeCheckEnter(tc);
    }

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
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	return jl().typeCheck(tc);
    }

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
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec) throws SemanticException {
	return jl().exceptionCheckEnter(ec);
    }

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
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	return jl().exceptionCheck(ec);
    }

    /**
     * Pretty-print the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param pp The pretty printer.  This is <i>not</i> a visitor.
     */
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        jl().prettyPrint(w, pp);
    }

    /**
     * Translate the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param tr The translation pass.  This is <i>not</i> a visitor.
     */
    public void translate(CodeWriter w, Translator tr) {
        jl().translate(w, tr);
    }
}
