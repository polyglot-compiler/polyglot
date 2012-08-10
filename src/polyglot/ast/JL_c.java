/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ConstantChecker;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * <code>JL_c</code> is the super class of JL node delegates objects.
 * It defines default implementations of the methods which implement compiler
 * passes, dispatching to the node to perform the actual work of the pass.
 * Language extensions may subclass <code>JL_c</code> for individual node
 * classes or may reimplement all compiler passes in a new class implementing
 * the <code>JL</code> interface.
 */
public class JL_c extends Ext_c implements JL {
    public JL_c() {
    }

    /** The <code>JL</code> object we dispatch to, by default, the node
     * itself, but possibly another delegate.
     */
    public JL jl() {
        return node();
    }

    /**
     * Visit the children of the node.
     *
     * @param v The visitor that will traverse/rewrite the AST.
     * @return A new AST if a change was made, or <code>this</code>.
     */
    @Override
    public Node visitChildren(NodeVisitor v) {
        return jl().visitChildren(v);
    }

    /**
     * Push a new scope upon entering this node, and add any declarations to the
     * context that should be in scope when visiting children of this node.
     * This should <i>not</i> update the old context
     * imperatively.  Use <code>addDecls</code> when leaving the node
     * for that.
     * @param c the current <code>Context</code>
     * @return the <code>Context</code> to be used for visiting this node. 
     */
    @Override
    public Context enterScope(Context c) {
        return jl().enterScope(c);
    }

    /**
     * Push a new scope for visiting the child node <code>child</code>. 
     * The default behavior is to delegate the call to the child node, and let
     * it add appropriate declarations that should be in scope. However,
     * this method gives parent nodes have the ability to modify this behavior.
     * @param child the child node about to be entered.
     * @param c the current <code>Context</code>
     * @return the <code>Context</code> to be used for visiting node 
     *           <code>child</code>
     */
    @Override
    public Context enterChildScope(Node child, Context c) {
        return jl().enterChildScope(child, c);
    }

    /**
     * Add any declarations to the context that should be in scope when
     * visiting later sibling nodes.
     * @param c The context to which to add declarations.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        return jl().disambiguateOverride(parent, ar);
    }

    @Override
    public NodeVisitor disambiguateEnter(AmbiguityRemover ar)
            throws SemanticException {
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
    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        return jl().disambiguate(ar);
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
    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        return jl().typeCheckOverride(parent, tc);
    }

    @Override
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
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return jl().typeCheck(tc);
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        return jl().checkConstants(cc);
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
    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
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
    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        return jl().exceptionCheck(ec);
    }

    /** 
     * List of Types of exceptions that might get thrown.  The result is
     * not necessarily correct until after type checking. 
     */
    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        return jl().throwTypes(ts);
    }

    /** Dump the AST for debugging. */
    @Override
    public void dump(OutputStream os) {
        jl().dump(os);
    }

    /** Dump the AST for debugging. */
    @Override
    public void dump(Writer w) {
        jl().dump(w);
    }

    /** Pretty-print the AST for debugging. */
    @Override
    public void prettyPrint(OutputStream os) {
        jl().prettyPrint(os);
    }

    /** Pretty-print the AST for debugging. */
    @Override
    public void prettyPrint(Writer w) {
        jl().prettyPrint(w);
    }

    /**
     * Pretty-print the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param pp The pretty printer.  This is <i>not</i> a visitor.
     */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        jl().prettyPrint(w, pp);
    }

    /**
     * Translate the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param tr The translation pass.  This is <i>not</i> a visitor.
     */
    @Override
    public void translate(CodeWriter w, Translator tr) {
        jl().translate(w, tr);
    }

    @Override
    public Node copy(NodeFactory nf) {
        return jl().copy(nf);
    }

    @Override
    public Node copy(ExtensionInfo extInfo) throws SemanticException {
        return jl().copy(extInfo);
    }

}
