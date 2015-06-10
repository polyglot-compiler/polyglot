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
import polyglot.translate.ExtensionRewriter;
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
 * {@code Ext} is the super type of all node extension objects.
 * It contains a pointer back to the node it is extending and a possibly-null
 * pointer to another extension node.
 */
public abstract class Ext_c implements Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Node node;
    protected NodeOps pred;
    protected Ext ext;

    public Ext_c() {
        this(null);
    }

    public Ext_c(Ext ext) {
        node = null;
        this.ext = ext;
    }

    @Override
    public Lang lang() {
        throw new InternalCompilerError("Unexpected invocation from extension object: "
                + this);
    }

    protected final JLang superLang() {
        return (JLang) pred.lang();
    }

    @Override
    public <N extends Node> N visitChild(N child, NodeVisitor v) {
        return node().visitChild(child, v);
    }

    @Override
    public <N extends Node> List<N> visitList(List<N> l, NodeVisitor v) {
        return node().visitList(l, v);
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public NodeOps pred() {
        return pred;
    }

    @Override
    public void init(Node node) {
        if (this.node != null)
            throw new InternalCompilerError("Already initialized.");
        this.node = node;
        if (ext != null) ext.init(node);
    }

    @Override
    public void initPred(NodeOps pred) {
        if (this.pred != null)
            throw new InternalCompilerError("Already initialized.");
        this.pred = pred;
        if (ext != null) ext.initPred(this);
    }

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
    public Ext copy() {
        try {
            Ext_c copy = (Ext_c) super.clone();
            if (ext != null) {
                copy.ext = ext.copy();
            }
            copy.node = null; // uninitialize
            copy.pred = null; // uninitialize
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Unable to clone an extension object.");
        }
    }

    @Override
    public String toString() {
        StringBuffer sb =
                new StringBuffer(StringUtil.getShortNameComponent(getClass().getName()));
        if (ext != null) {
            sb.append(":");
            sb.append(ext.toString());
        }
        return sb.toString();
    }

    @Override
    public void dump(CodeWriter w) {
        w.write(toString());
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        return superLang().visitChildren(node(), v);
    }

    @Override
    public Context enterScope(Context c) {
        return superLang().enterScope(node(), c);
    }

    @Override
    public Context enterChildScope(Node child, Context c) {
        return superLang().enterChildScope(node(), child, c);
    }

    @Override
    public void addDecls(Context c) {
        superLang().addDecls(node(), c);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        return superLang().buildTypesEnter(node(), tb);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return superLang().buildTypes(node(), tb);
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        return superLang().disambiguateOverride(node(), parent, ar);
    }

    @Override
    public NodeVisitor disambiguateEnter(AmbiguityRemover ar)
            throws SemanticException {
        return superLang().disambiguateEnter(node(), ar);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        return superLang().disambiguate(node(), ar);
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        return superLang().typeCheckOverride(node(), parent, tc);
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        return superLang().typeCheckEnter(node(), tc);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return superLang().typeCheck(node(), tc);
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        return superLang().childExpectedType(node(), child, av);
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        return superLang().checkConstants(node(), cc);
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        return superLang().exceptionCheckEnter(node(), ec);
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        return superLang().exceptionCheck(node(), ec);
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        return superLang().throwTypes(node(), ts);
    }

    @Override
    public NodeVisitor extRewriteEnter(ExtensionRewriter rw)
            throws SemanticException {
        return superLang().extRewriteEnter(node(), rw);
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        return superLang().extRewrite(node(), rw);
    }

    @Override
    public Node extRewriteOverride(ExtensionRewriter rw) {
        return superLang().extRewriteOverride(node(), rw);
    }

    @Deprecated
    @Override
    public void dump(OutputStream os) {
        node().del().dump(os);
    }

    @Override
    public void dump(Lang lang, OutputStream os) {
        superLang().dump(node(), lang, os);
    }

    @Deprecated
    @Override
    public void dump(Writer w) {
        node().del().dump(w);
    }

    @Override
    public void dump(Lang lang, Writer w) {
        superLang().dump(node(), lang, w);
    }

    @Deprecated
    @Override
    public void prettyPrint(OutputStream os) {
        node().del().prettyPrint(os);
    }

    @Override
    public void prettyPrint(Lang lang, OutputStream os) {
        superLang().prettyPrint(node(), lang, os);
    }

    @Deprecated
    @Override
    public void prettyPrint(Writer w) {
        node().del().prettyPrint(w);
    }

    @Override
    public void prettyPrint(Lang lang, Writer w) {
        superLang().prettyPrint(node(), lang, w);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        superLang().prettyPrint(node(), w, pp);
    }

    public void print(Node child, CodeWriter w, PrettyPrinter pp) {
        pp.print(node(), child, w);
    }

    public void printBlock(Node n, CodeWriter w, PrettyPrinter pp) {
        w.begin(0);
        print(n, w, pp);
        w.end();
    }

    public void printSubStmt(Stmt stmt, CodeWriter w, PrettyPrinter pp) {
        if (stmt instanceof Block) {
            w.write(" ");
            print(stmt, w, pp);
        }
        else {
            w.allowBreak(4, " ");
            printBlock(stmt, w, pp);
        }
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        superLang().translate(node(), w, tr);
    }

    @Override
    public Node copy(NodeFactory nf) {
        return superLang().copy(node(), nf);
    }

    @Override
    public Node copy(ExtensionInfo extInfo) throws SemanticException {
        return superLang().copy(node(), extInfo);
    }
}
