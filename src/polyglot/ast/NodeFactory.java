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

import java.util.List;

import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.util.Position;

/**
 * A <code>NodeFactory</code> constructs AST nodes.  All node construction
 * should go through this factory or be done with the <code>copy()</code>
 * method of <code>Node</code>.
 */
public interface NodeFactory {

    /**
     * Returns a disambiguator for nodes from this factory.
     */
    Disamb disamb();

    //////////////////////////////////////////////////////////////////
    // Factory Methods
    //////////////////////////////////////////////////////////////////

    Id Id(Position pos, String id);

    AmbExpr AmbExpr(Position pos, Id name);

    /** @deprecated */
    @Deprecated
    AmbExpr AmbExpr(Position pos, String name);

    Expr ExprFromQualifiedName(Position pos, String qualifiedName);

    // type or expr
    AmbReceiver AmbReceiver(Position pos, Id name);

    AmbReceiver AmbReceiver(Position pos, Prefix prefix, Id name);

    /** @deprecated */
    @Deprecated
    AmbReceiver AmbReceiver(Position pos, String name);

    /** @deprecated */
    @Deprecated
    AmbReceiver AmbReceiver(Position pos, Prefix prefix, String name);

    Receiver ReceiverFromQualifiedName(Position pos, String qualifiedName);

    // package or type
    AmbQualifierNode AmbQualifierNode(Position pos, Id name);

    AmbQualifierNode AmbQualifierNode(Position pos, QualifierNode qual, Id name);

    /** @deprecated */
    @Deprecated
    AmbQualifierNode AmbQualifierNode(Position pos, String name);

    /** @deprecated */
    @Deprecated
    AmbQualifierNode AmbQualifierNode(Position pos, QualifierNode qual,
            String name);

    QualifierNode QualifierNodeFromQualifiedName(Position pos,
            String qualifiedName);

    // package or type or expr
    AmbPrefix AmbPrefix(Position pos, Id name);

    AmbPrefix AmbPrefix(Position pos, Prefix prefix, Id name);

    /** @deprecated */
    @Deprecated
    AmbPrefix AmbPrefix(Position pos, String name);

    /** @deprecated */
    @Deprecated
    AmbPrefix AmbPrefix(Position pos, Prefix prefix, String name);

    Prefix PrefixFromQualifiedName(Position pos, String qualifiedName);

    AmbTypeNode AmbTypeNode(Position pos, Id name);

    AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier, Id name);

    /** @deprecated */
    @Deprecated
    AmbTypeNode AmbTypeNode(Position pos, String name);

    /** @deprecated */
    @Deprecated
    AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier, String name);

    TypeNode TypeNodeFromQualifiedName(Position pos, String qualifiedName);

    ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base);

    CanonicalTypeNode CanonicalTypeNode(Position pos, Type type);

    ArrayAccess ArrayAccess(Position pos, Expr base, Expr index);

    ArrayInit ArrayInit(Position pos);

    ArrayInit ArrayInit(Position pos, List<Expr> elements);

    Assert Assert(Position pos, Expr cond);

    Assert Assert(Position pos, Expr cond, Expr errorMessage);

    Assign Assign(Position pos, Expr target, Assign.Operator op, Expr source);

    LocalAssign LocalAssign(Position pos, Local target, Assign.Operator op,
            Expr source);

    FieldAssign FieldAssign(Position pos, Field target, Assign.Operator op,
            Expr source);

    ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess target,
            Assign.Operator op, Expr source);

    AmbAssign AmbAssign(Position pos, Expr target, Assign.Operator op,
            Expr source);

    Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right);

    Block Block(Position pos, Stmt... statements);

    Block Block(Position pos, List<Stmt> statements);

    SwitchBlock SwitchBlock(Position pos, List<Stmt> statements);

    BooleanLit BooleanLit(Position pos, boolean value);

    Branch Break(Position pos);

    Branch Break(Position pos, Id label);

    /** @deprecated */
    @Deprecated
    Branch Break(Position pos, String label);

    Branch Continue(Position pos);

    Branch Continue(Position pos, Id label);

    /** @deprecated */
    @Deprecated
    Branch Continue(Position pos, String label);

    Branch Branch(Position pos, Branch.Kind kind);

    Branch Branch(Position pos, Branch.Kind kind, Id label);

    /** @deprecated */
    @Deprecated
    Branch Branch(Position pos, Branch.Kind kind, String label);

    Call Call(Position pos, Id name, Expr... args);

    Call Call(Position pos, Id name, List<Expr> args);

    Call Call(Position pos, Receiver target, Id name, Expr... args);

    Call Call(Position pos, Receiver target, Id name, List<Expr> args);

    /** @deprecated */
    @Deprecated
    Call Call(Position pos, String name, Expr... args);

    /** @deprecated */
    @Deprecated
    Call Call(Position pos, String name, List<Expr> args);

    /** @deprecated */
    @Deprecated
    Call Call(Position pos, Receiver target, String name, Expr... args);

    /** @deprecated */
    @Deprecated
    Call Call(Position pos, Receiver target, String name, List<Expr> args);

    Case Default(Position pos);

    Case Case(Position pos, Expr expr);

    Cast Cast(Position pos, TypeNode type, Expr expr);

    Catch Catch(Position pos, Formal formal, Block body);

    CharLit CharLit(Position pos, char value);

    ClassBody ClassBody(Position pos, List<ClassMember> members);

    ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body);

    /** @deprecated */
    @Deprecated
    ClassDecl ClassDecl(Position pos, Flags flags, String name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body);

    ClassLit ClassLit(Position pos, TypeNode typeNode);

    Conditional Conditional(Position pos, Expr cond, Expr consequent,
            Expr alternative);

    ConstructorCall ThisCall(Position pos, List<Expr> args);

    ConstructorCall ThisCall(Position pos, Expr outer, List<Expr> args);

    ConstructorCall SuperCall(Position pos, List<Expr> args);

    ConstructorCall SuperCall(Position pos, Expr outer, List<Expr> args);

    ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind,
            List<Expr> args);

    ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind,
            Expr outer, List<Expr> args);

    ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body);

    /** @deprecated */
    @Deprecated
    ConstructorDecl ConstructorDecl(Position pos, Flags flags, String name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body);

    FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type, Id name);

    FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type, Id name,
            Expr init);

    /** @deprecated */
    @Deprecated
    FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type, String name);

    /** @deprecated */
    @Deprecated
    FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type, String name,
            Expr init);

    Do Do(Position pos, Stmt body, Expr cond);

    Empty Empty(Position pos);

    Eval Eval(Position pos, Expr expr);

    Field Field(Position pos, Id name);

    Field Field(Position pos, Receiver target, Id name);

    /** @deprecated */
    @Deprecated
    Field Field(Position pos, String name);

    /** @deprecated */
    @Deprecated
    Field Field(Position pos, Receiver target, String name);

    FloatLit FloatLit(Position pos, FloatLit.Kind kind, double value);

    For For(Position pos, List<ForInit> inits, Expr cond,
            List<ForUpdate> iters, Stmt body);

    Formal Formal(Position pos, Flags flags, TypeNode type, Id name);

    /** @deprecated */
    @Deprecated
    Formal Formal(Position pos, Flags flags, TypeNode type, String name);

    If If(Position pos, Expr cond, Stmt consequent);

    If If(Position pos, Expr cond, Stmt consequent, Stmt alternative);

    Import Import(Position pos, Import.Kind kind, String name);

    Initializer Initializer(Position pos, Flags flags, Block body);

    Instanceof Instanceof(Position pos, Expr expr, TypeNode type);

    IntLit IntLit(Position pos, IntLit.Kind kind, long value);

    Labeled Labeled(Position pos, Id label, Stmt body);

    /** @deprecated */
    @Deprecated
    Labeled Labeled(Position pos, String label, Stmt body);

    Local Local(Position pos, Id name);

    /** @deprecated */
    @Deprecated
    Local Local(Position pos, String name);

    LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl);

    LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, Id name);

    LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, Id name,
            Expr init);

    /** @deprecated */
    @Deprecated
    LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, String name);

    /** @deprecated */
    @Deprecated
    LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, String name,
            Expr init);

    MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType,
            Id name, List<Formal> formals, List<TypeNode> throwTypes, Block body);

    /** @deprecated */
    @Deprecated
    MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType,
            String name, List<Formal> formals, List<TypeNode> throwTypes,
            Block body);

    New New(Position pos, TypeNode type, List<Expr> args);

    New New(Position pos, TypeNode type, List<Expr> args, ClassBody body);

    New New(Position pos, Expr outer, TypeNode objectType, List<Expr> args);

    New New(Position pos, Expr outer, TypeNode objectType, List<Expr> args,
            ClassBody body);

    NewArray NewArray(Position pos, TypeNode base, List<Expr> dims);

    NewArray NewArray(Position pos, TypeNode base, List<Expr> dims, int addDims);

    NewArray NewArray(Position pos, TypeNode base, int addDims, ArrayInit init);

    NewArray NewArray(Position pos, TypeNode base, List<Expr> dims,
            int addDims, ArrayInit init);

    NullLit NullLit(Position pos);

    Return Return(Position pos);

    Return Return(Position pos, Expr expr);

    SourceCollection SourceCollection(Position pos, List<SourceFile> sources);

    SourceFile SourceFile(Position pos, List<TopLevelDecl> decls);

    SourceFile SourceFile(Position pos, List<Import> imports,
            List<TopLevelDecl> decls);

    SourceFile SourceFile(Position pos, PackageNode packageName,
            List<Import> imports, List<TopLevelDecl> decls);

    Special This(Position pos);

    Special This(Position pos, TypeNode outer);

    Special Super(Position pos);

    Special Super(Position pos, TypeNode outer);

    Special Special(Position pos, Special.Kind kind);

    Special Special(Position pos, Special.Kind kind, TypeNode outer);

    StringLit StringLit(Position pos, String value);

    Switch Switch(Position pos, Expr expr, List<SwitchElement> elements);

    Synchronized Synchronized(Position pos, Expr expr, Block body);

    Throw Throw(Position pos, Expr expr);

    Try Try(Position pos, Block tryBlock, List<Catch> catchBlocks);

    Try Try(Position pos, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock);

    PackageNode PackageNode(Position pos, Package p);

    Unary Unary(Position pos, Unary.Operator op, Expr expr);

    Unary Unary(Position pos, Expr expr, Unary.Operator op);

    While While(Position pos, Expr cond, Stmt body);
}
