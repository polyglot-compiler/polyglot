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
import java.io.Serializable;
import java.io.Writer;
import java.util.List;

import polyglot.frontend.ExtensionInfo;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
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
 * {@code JL_c} is the super class of JL node delegate objects.
 * It defines default implementations of the methods which implement compiler
 * passes, dispatching to the node to perform the actual work of the pass.
 * Language extensions may subclass {@code JL_c} for individual node
 * classes or may reimplement all compiler passes in a new class implementing
 * the {@code JL} interface.
 */
@Deprecated
public class JLDel_c implements JLDel, Serializable {
    private static final long serialVersionUID = SerialVersionUID.generate();
    public static final JLDel instance = new JLDel_c();

    Node node;

    /** Create an uninitialized delegate. It must be initialized using the init() method.
     */
    public JLDel_c() {
    }

    /** The {@code NodeOps} object we dispatch to, by default, the node
     * itself, but possibly another delegate.
     */
    public NodeOps jl() {
        return node();
    }

    @Override
    public void init(Node n) {
        assert node == null;
        node = n;
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public JLDel copy() {
        try {
            JLDel_c copy = (JLDel_c) super.clone();
            copy.node = null; // uninitialize
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Unable to clone a delegate");
        }
    }

    @Override
    public final JLang lang() {
        throw new InternalCompilerError("Unsupported method lang() for delegates");
    }

    @Override
    public <N extends Node> N visitChild(N child, NodeVisitor v) {
        return jl().visitChild(child, v);
    }

    @Override
    public <N extends Node> List<N> visitList(List<N> l, NodeVisitor v) {
        return jl().visitList(l, v);
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        return jl().visitChildren(v);
    }

    @Override
    public Context enterScope(Context c) {
        return jl().enterScope(c);
    }

    @Override
    public Context enterChildScope(Node child, Context c) {
        return jl().enterChildScope(child, c);
    }

    @Override
    public void addDecls(Context c) {
        jl().addDecls(c);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        return jl().buildTypesEnter(tb);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return jl().buildTypes(tb);
    }

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

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        return jl().disambiguate(ar);
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        return jl().typeCheckOverride(parent, tc);
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        return jl().typeCheckEnter(tc);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return jl().typeCheck(tc);
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        return jl().childExpectedType(child, av);
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        return jl().checkConstants(cc);
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        return jl().exceptionCheckEnter(ec);
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        return jl().exceptionCheck(ec);
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        return jl().throwTypes(ts);
    }

    @Override
    public NodeVisitor extRewriteEnter(ExtensionRewriter rw)
            throws SemanticException {
        ToExt ext = rw.from_ext().getToExt(rw.to_ext(), node());
        return ext.toExtEnter(rw);
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        ToExt ext = rw.from_ext().getToExt(rw.to_ext(), node());
        return ext.toExt(rw);
    }

    @Override
    public Node extRewriteOverride(ExtensionRewriter rw) {
        throw new InternalCompilerError("Shouldn't be here.");
    }

    @Deprecated
    @Override
    public void dump(OutputStream os) {
        jl().dump(os);
    }

    @Override
    public void dump(Lang lang, OutputStream os) {
        jl().dump(lang, os);
    }

    @Deprecated
    @Override
    public void dump(Writer w) {
        jl().dump(w);
    }

    @Override
    public void dump(Lang lang, Writer w) {
        jl().dump(lang, w);
    }

    @Deprecated
    @Override
    public void prettyPrint(OutputStream os) {
        jl().prettyPrint(os);
    }

    @Override
    public void prettyPrint(Lang lang, OutputStream os) {
        jl().prettyPrint(lang, os);
    }

    @Deprecated
    @Override
    public void prettyPrint(Writer w) {
        jl().prettyPrint(w);
    }

    @Override
    public void prettyPrint(Lang lang, Writer w) {
        jl().prettyPrint(lang, w);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        jl().prettyPrint(w, pp);
    }

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
