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
 * A <code>Node</code> represents an AST node.  All AST nodes must implement
 * this interface.  Nodes should be immutable: methods which set fields
 * of the node should copy the node, set the field in the copy, and then
 * return the copy.
 */
public interface NodeOps {
    /**
     * Visit the children of the node.
     *
     * @param v The visitor that will traverse/rewrite the AST.
     * @return A new AST if a change was made, or <code>this</code>.
     */
    Node visitChildren(NodeVisitor v);

    /**
     * Push a new scope upon entering this node, and add any declarations to the
     * context that should be in scope when visiting children of this node.
     * This should <i>not</i> update the old context
     * imperatively.  Use <code>addDecls</code> when leaving the node
     * for that.
     * @param c the current <code>Context</code>
     * @return the <code>Context</code> to be used for visiting this node. 
     */
    public Context enterScope(Context c);

    /**
     * Push a new scope for visiting the child node <code>child</code>. 
     * The default behavior is to delegate the call to the child node, and let
     * it add appropriate declarations that should be in scope. However,
     * this method gives parent nodes have the ability to modify this behavior.
     * @param child The child node about to be entered.
     * @param c The current <code>Context</code>
     * @return the <code>Context</code> to be used for visiting node 
     *           <code>child</code>
     */
    public Context enterChildScope(Node child, Context c);

    /**
     * Add any declarations to the context that should be in scope when
     * visiting later sibling nodes.
     * @param c The context to which to add declarations.
     */
    void addDecls(Context c);

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
     * Disambiguate the AST.
     *
     * This method is called by the <code>override()</code> method of the
     * visitor.  If this method returns non-null, the node's children
     * will not be visited automatically.  Thus, the method should check
     * both the node <code>this</code> and it's children, usually by
     * invoking <code>visitChildren</code> with <code>tc</code> or
     * with another visitor, returning a non-null node.  OR, the method
     * should do nothing and simply return <code>null</code> to allow
     * <code>enter</code>, <code>visitChildren</code>, and <code>leave</code>
     * to be invoked on the node.
     *
     * The default implementation returns <code>null</code>.
     * Overriding of this method is discouraged, but sometimes necessary.
     *
     * @param ar The visitor which disambiguates.
     */
    Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException;

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
     * The node should not assume that its children have been disambiguated.
     * If it depends on a child being disambiguated,
     * it may just return <code>this</code> without doing any work.
     *
     * @param ar The visitor which disambiguates.
     */
    Node disambiguate(AmbiguityRemover ar) throws SemanticException;

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
     * This method is called by the <code>override()</code> method of the
     * visitor.  If this method returns non-null, the node's children
     * will not be visited automatically.  Thus, the method should check
     * both the node <code>this</code> and it's children, usually by
     * invoking <code>visitChildren</code> with <code>tc</code> or
     * with another visitor, returning a non-null node.  OR, the method
     * should do nothing and simply return <code>null</code> to allow
     * <code>enter</code>, <code>visitChildren</code>, and <code>leave</code>
     * to be invoked on the node.
     *
     * The default implementation returns <code>null</code>.
     * Overriding of this method is discouraged, but sometimes necessary.
     *
     * @param tc The type checking visitor.
     */
    Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException;

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
     * Check if the node is a compile-time constant.
     *
     * This method is called by the <code>leave()</code> method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * <code>this</code> or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param cc The constant checking visitor.
     */
    Node checkConstants(ConstantChecker cc) throws SemanticException;

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
    NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException;

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
     * List of Types of exceptions that might get thrown.  The result is
     * not necessarily correct until after type checking. 
     */
    List<Type> throwTypes(TypeSystem ts);

    /** Dump the AST for debugging. */
    public void dump(OutputStream os);

    /** Dump the AST for debugging. */
    public void dump(Writer w);

    /** Pretty-print the AST for debugging. */
    public void prettyPrint(OutputStream os);

    /** Pretty-print the AST for debugging. */
    public void prettyPrint(Writer w);

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

    /**
     * Produce a copy of this node using the given NodeFactory.
     */
    Node copy(NodeFactory nf);

    /**
     * Produce a copy of this node using the given ExtensionInfo.
     * This will typically be implemented by calling
     * copy(NodeFactory nf), and then potentially copying over
     * type information.
     * @throws SemanticException If the type information cannot be copied.
     */
    Node copy(ExtensionInfo extInfo) throws SemanticException;

}
