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
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ConstantChecker;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * {@code JLang_c} defines the dispatching mechanism to methods which implement
 * Java compiler operations.  Language extensions may override methods that
 * determine the appropriate object which implements AST operations (such as
 * NodeOps) to redirect dispatching to appropriate extension code.
 */
public class JLang_c implements JLang {
    public static final JLang instance = new JLang_c();

    protected JLang_c() {
    }

    protected NodeOps NodeOps(Node n) {
        return n;
    }

    protected CallOps CallOps(Call n) {
        return (CallOps) n;
    }

    protected ClassDeclOps ClassDeclOps(ClassDecl n) {
        return (ClassDeclOps) n;
    }

    protected ExprOps ExprOps(Expr n) {
        return (ExprOps) n;
    }

    protected LoopOps LoopOps(Loop n) {
        return (LoopOps) n;
    }

    protected NewOps NewOps(New n) {
        return (NewOps) n;
    }

    protected ProcedureCallOps ProcedureCallOps(ProcedureCall n) {
        return (ProcedureCallOps) n;
    }

    protected ProcedureDeclOps ProcedureDeclOps(ProcedureDecl n) {
        return (ProcedureDeclOps) n;
    }

    protected TermOps TermOps(Term n) {
        return (TermOps) n;
    }

    protected TryOps TryOps(Try n) {
        return (TryOps) n;
    }

    // NodeOps

    @Override
    public final Node visitChildren(Node n, NodeVisitor v) {
        return NodeOps(n).visitChildren(v);
    }

    @Override
    public final Context enterScope(Node n, Context c) {
        return NodeOps(n).enterScope(c);
    }

    @Override
    public final Context enterChildScope(Node n, Node child, Context c) {
        return NodeOps(n).enterChildScope(child, c);
    }

    @Override
    public final void addDecls(Node n, Context c) {
        NodeOps(n).addDecls(c);
    }

    @Override
    public final NodeVisitor buildTypesEnter(Node n, TypeBuilder tb)
            throws SemanticException {
        return NodeOps(n).buildTypesEnter(tb);
    }

    @Override
    public final Node buildTypes(Node n, TypeBuilder tb)
            throws SemanticException {
        return NodeOps(n).buildTypes(tb);
    }

    @Override
    public final Node disambiguateOverride(Node n, Node parent,
            AmbiguityRemover ar) throws SemanticException {
        return NodeOps(n).disambiguateOverride(parent, ar);
    }

    @Override
    public final NodeVisitor disambiguateEnter(Node n, AmbiguityRemover ar)
            throws SemanticException {
        return NodeOps(n).disambiguateEnter(ar);
    }

    @Override
    public final Node disambiguate(Node n, AmbiguityRemover ar)
            throws SemanticException {
        return NodeOps(n).disambiguate(ar);
    }

    @Override
    public final NodeVisitor typeCheckEnter(Node n, TypeChecker tc)
            throws SemanticException {
        return NodeOps(n).typeCheckEnter(tc);
    }

    @Override
    public final Node typeCheckOverride(Node n, Node parent, TypeChecker tc)
            throws SemanticException {
        return NodeOps(n).typeCheckOverride(parent, tc);
    }

    @Override
    public final Node typeCheck(Node n, TypeChecker tc)
            throws SemanticException {
        return NodeOps(n).typeCheck(tc);
    }

    @Override
    public final Type childExpectedType(Node n, Expr child, AscriptionVisitor av) {
        return NodeOps(n).childExpectedType(child, av);
    }

    @Override
    public final Node checkConstants(Node n, ConstantChecker cc)
            throws SemanticException {
        return NodeOps(n).checkConstants(cc);
    }

    @Override
    public final NodeVisitor exceptionCheckEnter(Node n, ExceptionChecker ec)
            throws SemanticException {
        return NodeOps(n).exceptionCheckEnter(ec);
    }

    @Override
    public final Node exceptionCheck(Node n, ExceptionChecker ec)
            throws SemanticException {
        return NodeOps(n).exceptionCheck(ec);
    }

    @Override
    public final List<Type> throwTypes(Node n, TypeSystem ts) {
        return NodeOps(n).throwTypes(ts);
    }

    @Override
    public final NodeVisitor extRewriteEnter(Node n, ExtensionRewriter rw)
            throws SemanticException {
        return NodeOps(n).extRewriteEnter(rw);
    }

    @Override
    public final Node extRewrite(Node n, ExtensionRewriter rw)
            throws SemanticException {
        return NodeOps(n).extRewrite(rw);
    }

    @Override
    public final Node extRewriteOverride(Node n, ExtensionRewriter rw) {
        return NodeOps(n).extRewriteOverride(rw);
    }

    @Override
    public final void dump(Node n, Lang lang, OutputStream os) {
        NodeOps(n).dump(lang, os);
    }

    @Override
    public final void dump(Node n, Lang lang, Writer w) {
        NodeOps(n).dump(lang, w);
    }

    @Override
    public final void prettyPrint(Node n, Lang lang, OutputStream os) {
        NodeOps(n).prettyPrint(lang, os);
    }

    @Override
    public final void prettyPrint(Node n, Lang lang, Writer w) {
        NodeOps(n).prettyPrint(lang, w);
    }

    @Override
    public final void prettyPrint(Node n, CodeWriter w, PrettyPrinter pp) {
        NodeOps(n).prettyPrint(w, pp);
    }

    @Override
    public final void translate(Node n, CodeWriter w, Translator tr) {
        NodeOps(n).translate(w, tr);
    }

    @Override
    public final Node copy(Node n, NodeFactory nf) {
        return NodeOps(n).copy(nf);
    }

    @Override
    public final Node copy(Node n, ExtensionInfo extInfo)
            throws SemanticException {
        return NodeOps(n).copy(extInfo);
    }

    // CallOps

    @Override
    public final Type findContainer(Call n, TypeSystem ts, MethodInstance mi) {
        return CallOps(n).findContainer(ts, mi);
    }

    @Override
    public final ReferenceType findTargetType(Call n) throws SemanticException {
        return CallOps(n).findTargetType();
    }

    @Override
    public final Node typeCheckNullTarget(Call n, TypeChecker tc,
            List<Type> argTypes) throws SemanticException {
        return CallOps(n).typeCheckNullTarget(tc, argTypes);
    }

    // ClassDeclOps

    @Override
    public final void prettyPrintHeader(ClassDecl n, CodeWriter w,
            PrettyPrinter tr) {
        ClassDeclOps(n).prettyPrintHeader(w, tr);
    }

    @Override
    public final void prettyPrintFooter(ClassDecl n, CodeWriter w,
            PrettyPrinter tr) {
        ClassDeclOps(n).prettyPrintFooter(w, tr);
    }

    @Override
    public final Node addDefaultConstructor(ClassDecl n, TypeSystem ts,
            NodeFactory nf, ConstructorInstance defaultConstructorInstance)
            throws SemanticException {
        return ClassDeclOps(n).addDefaultConstructor(ts,
                                                     nf,
                                                     defaultConstructorInstance);
    }

    // ExprOps

    @Override
    public final boolean constantValueSet(Expr n, Lang lang) {
        return ExprOps(n).constantValueSet(lang);
    }

    @Override
    public final boolean isConstant(Expr n, Lang lang) {
        return ExprOps(n).isConstant(lang);
    }

    @Override
    public final Object constantValue(Expr n, Lang lang) {
        return ExprOps(n).constantValue(lang);
    }

    // LoopOps

    @Override
    public final boolean condIsConstant(Loop n, JLang lang) {
        return LoopOps(n).condIsConstant(lang);
    }

    @Override
    public final boolean condIsConstantTrue(Loop n, JLang lang) {
        return LoopOps(n).condIsConstantTrue(lang);
    }

    @Override
    public final boolean condIsConstantFalse(Loop n, JLang lang) {
        return LoopOps(n).condIsConstantFalse(lang);
    }

    @Override
    public final Term continueTarget(Loop n) {
        return LoopOps(n).continueTarget();
    }

    // NewOps

    @Override
    public final TypeNode findQualifiedTypeNode(New n, AmbiguityRemover ar,
            ClassType outer, TypeNode objectType) throws SemanticException {
        return NewOps(n).findQualifiedTypeNode(ar, outer, objectType);
    }

    @Override
    public final Expr findQualifier(New n, AmbiguityRemover ar, ClassType ct)
            throws SemanticException {
        return NewOps(n).findQualifier(ar, ct);
    }

    @Override
    public final void typeCheckFlags(New n, TypeChecker tc)
            throws SemanticException {
        NewOps(n).typeCheckFlags(tc);
    }

    @Override
    public final void typeCheckNested(New n, TypeChecker tc)
            throws SemanticException {
        NewOps(n).typeCheckNested(tc);
    }

    @Override
    public final void printQualifier(New n, CodeWriter w, PrettyPrinter tr) {
        NewOps(n).printQualifier(w, tr);
    }

    @Override
    public final void printShortObjectType(New n, CodeWriter w, PrettyPrinter tr) {
        NewOps(n).printShortObjectType(w, tr);
    }

    @Override
    public final void printBody(New n, CodeWriter w, PrettyPrinter tr) {
        NewOps(n).printBody(w, tr);
    }

    @Override
    public final ClassType findEnclosingClass(New n, Context c, ClassType ct) {
        return NewOps(n).findEnclosingClass(c, ct);
    }

    // ProcedureCallOps

    @Override
    public final void printArgs(ProcedureCall n, CodeWriter w, PrettyPrinter tr) {
        ProcedureCallOps(n).printArgs(w, tr);
    }

    // ProcedureDeclOps

    @Override
    public final void prettyPrintHeader(ProcedureDecl n, Flags flags,
            CodeWriter w, PrettyPrinter tr) {
        ProcedureDeclOps(n).prettyPrintHeader(flags, w, tr);
    }

    // TermOps

    @Override
    public final Term firstChild(Term n) {
        return TermOps(n).firstChild();
    }

    @Override
    public final <T> List<T> acceptCFG(Term n, CFGBuilder<?> v, List<T> succs) {
        return TermOps(n).acceptCFG(v, succs);
    }

    // TryOps

    @Override
    public final ExceptionChecker constructTryBlockExceptionChecker(Try n,
            ExceptionChecker ec) {
        return TryOps(n).constructTryBlockExceptionChecker(ec);
    }

    @Override
    public final Block exceptionCheckTryBlock(Try n, ExceptionChecker ec)
            throws SemanticException {
        return TryOps(n).exceptionCheckTryBlock(ec);
    }

    @Override
    public final List<Catch> exceptionCheckCatchBlocks(Try n,
            ExceptionChecker ec) throws SemanticException {
        return TryOps(n).exceptionCheckCatchBlocks(ec);
    }

    @Override
    public final Block exceptionCheckFinallyBlock(Try n, ExceptionChecker ec)
            throws SemanticException {
        return TryOps(n).exceptionCheckFinallyBlock(ec);
    }
}
