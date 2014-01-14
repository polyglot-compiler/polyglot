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
import java.util.List;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.util.StringUtil;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.ConstantChecker;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 * It contains a pointer back to the node it is extending and a possibly-null
 * pointer to another extension node. 
 */
public abstract class Ext_c implements Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Node node;
    protected Ext ext;
    protected JLang superLang = null;

    public Ext_c(Ext ext) {
        this.node = null;
        this.ext = ext;
    }

    public Ext_c() {
        this(null);
        this.node = null;
    }

    /** Initialize the extension object's pointer back to the node.
     * This also initializes the back pointers for all extensions of
     * the extension.
     */
    @Override
    public void init(Node node) {
        if (this.node != null) {
            throw new InternalCompilerError("Already initialized.");
        }

        this.node = node;

        if (this.ext != null) {
            this.ext.init(node);
        }
    }

    /**
     * Return the node we ultimately extend.
     */
    @Override
    public Node node() {
        return node;
    }

    /**
     * Return our extension object, or null.
     */
    @Override
    public Ext ext() {
        return ext;
    }

    @Override
    public Ext ext(Ext ext) {
        Ext old = this.ext;
        this.ext = null;

        Ext_c copy = (Ext_c) copy();

        copy.ext = ext;

        this.ext = old;

        return copy;
    }

    @Override
    public JLang superLang() {
        if (this.superLang == null) {
            return JLang_c.instance;
        }
        return this.superLang;
    }

    public void superLang(JLang superLang) {
        this.superLang = superLang;
    }

    /**
     * Copy the extension.
     */
    @Override
    public Object copy() {
        try {
            Ext_c copy = (Ext_c) super.clone();
            if (ext != null) {
                copy.ext = (Ext) ext.copy();
            }
            copy.node = null; // uninitialize
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Unable to clone an extension object.");
        }
    }

    @Override
    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName());
    }

    /**
     * Dump the AST node for debugging purposes.
     */
    @Override
    public void dump(CodeWriter w) {
        w.write(toString());
    }

    /**
     * Visit the children of the node.
     *
     * @param v The visitor that will traverse/rewrite the AST.
     * @return A new AST if a change was made, or <code>this</code>.
     */
    @Override
    public Node visitChildren(NodeVisitor v) {
        return node().visitChildren(v);
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
        return node().enterScope(c);
    }

    @Deprecated
    @Override
    public Context enterChildScope(Node child, Context c) {
        return node().enterChildScope(child, c);
    }

    @Override
    public Context enterChildScope(Lang lang, Node child, Context c) {
        return node().enterChildScope(lang, child, c);
    }

    /**
     * Add any declarations to the context that should be in scope when
     * visiting later sibling nodes.
     * @param c The context to which to add declarations.
     */
    @Override
    public void addDecls(Context c) {
        node().addDecls(c);
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
        return node().buildTypesEnter(tb);
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
        return node().buildTypes(tb);
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
        return node().disambiguateOverride(parent, ar);
    }

    @Override
    public NodeVisitor disambiguateEnter(AmbiguityRemover ar)
            throws SemanticException {
        return node().disambiguateEnter(ar);
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
        return node().disambiguate(ar);
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
        return node().typeCheckOverride(parent, tc);
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        return node().typeCheckEnter(tc);
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
        return node().typeCheck(tc);
    }

    /**
     * Get the expected type of a child expression of <code>this</code>.
     * The expected type is determined by the context in that the child occurs
     * (e.g., for <code>x = e</code>, the expected type of <code>e</code> is
     * the declared type of <code>x</code>.
     *
     * The expected type should impose the least constraints on the child's
     * type that are allowed by the parent node.
     *
     * @param child A child expression of this node.
     * @param av An ascription visitor.
     * @return The expected type of <code>child</code>.
     */
    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        return node().childExpectedType(child, av);
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        return node().checkConstants(cc);
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
        return node().exceptionCheckEnter(ec);
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
        return node().exceptionCheck(ec);
    }

    /** 
     * List of Types of exceptions that might get thrown.  The result is
     * not necessarily correct until after type checking. 
     */
    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        return node().throwTypes(ts);
    }

    @Deprecated
    @Override
    public void dump(OutputStream os) {
        node().del().dump(os);
    }

    @Override
    public void dump(Lang lang, OutputStream os) {
        node().dump(lang, os);
    }

    @Deprecated
    @Override
    public void dump(Writer w) {
        node().del().dump(w);
    }

    @Override
    public void dump(Lang lang, Writer w) {
        node().dump(lang, w);
    }

    @Deprecated
    @Override
    public void prettyPrint(OutputStream os) {
        node().del().prettyPrint(os);
    }

    @Override
    public void prettyPrint(Lang lang, OutputStream os) {
        node().prettyPrint(lang, os);
    }

    @Deprecated
    @Override
    public void prettyPrint(Writer w) {
        node().del().prettyPrint(w);
    }

    @Override
    public void prettyPrint(Lang lang, Writer w) {
        node().prettyPrint(lang, w);
    }

    /**
     * Pretty-print the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param pp The pretty printer.  This is <i>not</i> a visitor.
     */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        node().prettyPrint(w, pp);
    }

    /**
     * Translate the AST using the given code writer.
     *
     * @param w The code writer to which to write.
     * @param tr The translation pass.  This is <i>not</i> a visitor.
     */
    @Override
    public void translate(CodeWriter w, Translator tr) {
        node().translate(w, tr);
    }

    @Override
    public Node copy(NodeFactory nf) {
        return node().copy(nf);
    }

    @Override
    public Node copy(ExtensionInfo extInfo) throws SemanticException {
        return node().copy(extInfo);
    }
}
