/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.io.OutputStream;
import java.io.Writer;

import polyglot.frontend.ExtensionInfo;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * {@code Lang} contains methods implemented by an AST node.
 */
public interface Lang {
    /**
     * Visit the children of the node.
     *
     * @param v The visitor that will traverse/rewrite the AST.
     * @return A new AST if a change was made, or {@code this}.
     */
    Node visitChildren(Node n, NodeVisitor v);

    /**
     * Push a new scope upon entering this node, and add any declarations to the
     * context that should be in scope when visiting children of this node.
     * This should <i>not</i> update the old context
     * imperatively.  Use {@code addDecls} when leaving the node
     * for that.
     * @param c the current {@code Context}
     * @return the {@code Context} to be used for visiting this node.
     */
    Context enterScope(Node n, Context c);

    /**
     * Push a new scope for visiting the child node {@code child}.
     * The default behavior is to delegate the call to the child node, and let
     * it add appropriate declarations that should be in scope. However,
     * this method gives parent nodes have the ability to modify this behavior.
     * @param child The child node about to be entered.
     * @param c The current {@code Context}
     * @return the {@code Context} to be used for visiting node
     *           {@code child}
     */
    Context enterChildScope(Node n, Node child, Context c);

    /**
     * Add any declarations to the context that should be in scope when
     * visiting later sibling nodes.
     * @param c The context to which to add declarations.
     */
    void addDecls(Node n, Context c);

    /**
     * Collects classes, methods, and fields from the AST rooted at this node
     * and constructs type objects for these.  These type objects may be
     * ambiguous.  Inserts classes into the {@code TypeSystem}.
     *
     * This method is called by the {@code enter()} method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node on which
     * {@code visitChildren()} and {@code leave()} will be
     * invoked.
     *
     * @param tb The visitor which adds new type objects to the
     * {@code TypeSystem}.
     */
    NodeVisitor buildTypesEnter(Node n, TypeBuilder tb)
            throws SemanticException;

    /**
     * Collects classes, methods, and fields from the AST rooted at this node
     * and constructs type objects for these.  These type objects may be
     * ambiguous.  Inserts classes into the {@code TypeSystem}.
     *
     * This method is called by the {@code leave()} method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param tb The visitor which adds new type objects to the
     * {@code TypeSystem}.
     */
    Node buildTypes(Node n, TypeBuilder tb) throws SemanticException;

    /**
     * Type check the AST.
     *
     * This method is called by the {@code enter()} method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node on which
     * {@code visitChildren()} and {@code leave()} will be
     * invoked.
     *
     * @param tc The type checking visitor.
     */
    NodeVisitor typeCheckEnter(Node n, TypeChecker tc) throws SemanticException;

    /**
     * Type check the AST.
     *
     * This method is called by the {@code override()} method of the
     * visitor.  If this method returns non-null, the node's children
     * will not be visited automatically.  Thus, the method should check
     * both the node {@code this} and it's children, usually by
     * invoking {@code visitChildren} with {@code tc} or
     * with another visitor, returning a non-null node.  OR, the method
     * should do nothing and simply return {@code null} to allow
     * {@code enter}, {@code visitChildren}, and {@code leave}
     * to be invoked on the node.
     *
     * The default implementation returns {@code null}.
     * Overriding of this method is discouraged, but sometimes necessary.
     *
     * @param tc The type checking visitor.
     */
    Node typeCheckOverride(Node n, Node parent, TypeChecker tc)
            throws SemanticException;

    /**
     * Type check the AST.
     *
     * This method is called by the {@code leave()} method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param tc The type checking visitor.
     */
    Node typeCheck(Node n, TypeChecker tc) throws SemanticException;

    Node extRewriteOverride(Node n, ExtensionRewriter rw);

    /**
     * Rewrite the AST for the compilation in this language.
     *
     * This method is called by the {@code enter()} method of the
     * visitor.  The method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node on which
     * {@code visitChildren()} and {@code leave()} will be
     * invoked.
     *
     * @param rw The visitor.
     */
    NodeVisitor extRewriteEnter(Node n, ExtensionRewriter rw)
            throws SemanticException;

    /**
     * Rewrite the AST for the compilation in this language.
     *
     * This method is called by the {@code leave()} method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param rw The visitor.
     */
    Node extRewrite(Node n, ExtensionRewriter rw) throws SemanticException;

    /** Dump the AST for debugging. */
    void dump(Node n, Lang lang, OutputStream os);

    /** Dump the AST for debugging. */
    void dump(Node n, Lang lang, Writer w);

    /** Pretty-print the AST for debugging. */
    void prettyPrint(Node n, Lang lang, OutputStream os);

    /** Pretty-print the AST for debugging. */
    void prettyPrint(Node n, Lang lang, Writer w);

    /**
     * Pretty-print the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param pp The pretty printer.  This is <i>not</i> a visitor.
     */
    void prettyPrint(Node n, CodeWriter w, PrettyPrinter pp);

    /**
     * Translate the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param tr The translation pass.  This is <i>not</i> a visitor.
     */
    void translate(Node n, CodeWriter w, Translator tr);

    /**
     * Produce a copy of this node using the given NodeFactory.
     */
    Node copy(Node n, NodeFactory nf);

    /**
     * Produce a copy of this node using the given ExtensionInfo.
     * This will typically be implemented by calling
     * copy(NodeFactory nf), and then potentially copying over
     * type information.
     * @throws SemanticException If the type information cannot be copied.
     */
    Node copy(Node n, ExtensionInfo extInfo) throws SemanticException;

    // ExprOps

    /** Return true iff the compiler has determined whether this expression has a
     * constant value.  The value returned by {@code isConstant()} is valid only if
     * {@code constantValueSet()} is true. */
    boolean constantValueSet(Expr n, Lang lang);

    /**
     * Return whether the expression evaluates to a constant.
     * Requires that disambiguation has been done, and that
     * {@code constantValueSet()} is true.
     */
    boolean isConstant(Expr n, Lang lang);

    /** Return the constant value of the expression, if any.
     *  Requires that {@code isConstant()} is true.
     */
    Object constantValue(Expr n, Lang lang);

}
