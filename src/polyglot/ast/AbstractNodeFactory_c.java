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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polyglot.types.Flags;
import polyglot.util.Position;
import polyglot.util.StringUtil;

/**
 * This is a node factory that creates no nodes.  It, rather than
 * NodeFactory_c, should be subclassed by any extension which should
 * override the creation of <a>all</a> nodes.
 */
public abstract class AbstractNodeFactory_c implements NodeFactory {
    @Override
    public Disamb disamb() {
        return new Disamb_c();
    }

    @Override
    public Prefix PrefixFromQualifiedName(Position pos, String qualifiedName) {
        if (StringUtil.isNameShort(qualifiedName)) {
            return AmbPrefix(pos, null, qualifiedName);
        }

        String container = StringUtil.getPackageComponent(qualifiedName);
        String name = StringUtil.getShortNameComponent(qualifiedName);

        Position pos2 = pos.truncateEnd(name.length() + 1);

        return AmbPrefix(pos, PrefixFromQualifiedName(pos2, container), name);
    }

    @Override
    public TypeNode TypeNodeFromQualifiedName(Position pos, String qualifiedName) {
        if (StringUtil.isNameShort(qualifiedName)) {
            return AmbTypeNode(pos, null, qualifiedName);
        }

        String container = StringUtil.getPackageComponent(qualifiedName);
        String name = StringUtil.getShortNameComponent(qualifiedName);

        Position pos2 = pos.truncateEnd(name.length() + 1);

        return AmbTypeNode(pos,
                           QualifierNodeFromQualifiedName(pos2, container),
                           name);
    }

    @Override
    public Receiver ReceiverFromQualifiedName(Position pos, String qualifiedName) {
        if (StringUtil.isNameShort(qualifiedName)) {
            return AmbReceiver(pos, null, qualifiedName);
        }

        String container = StringUtil.getPackageComponent(qualifiedName);
        String name = StringUtil.getShortNameComponent(qualifiedName);

        Position pos2 = pos.truncateEnd(name.length() + 1);

        return AmbReceiver(pos, PrefixFromQualifiedName(pos2, container), name);

    }

    @Override
    public Expr ExprFromQualifiedName(Position pos, String qualifiedName) {
        if (StringUtil.isNameShort(qualifiedName)) {
            return AmbExpr(pos, qualifiedName);
        }

        String container = StringUtil.getPackageComponent(qualifiedName);
        String name = StringUtil.getShortNameComponent(qualifiedName);

        Position pos2 = pos.truncateEnd(name.length() + 1);

        return Field(pos, ReceiverFromQualifiedName(pos2, container), name);
    }

    @Override
    public QualifierNode QualifierNodeFromQualifiedName(Position pos,
            String qualifiedName) {
        if (StringUtil.isNameShort(qualifiedName)) {
            return AmbQualifierNode(pos, null, qualifiedName);
        }

        String container = StringUtil.getPackageComponent(qualifiedName);
        String name = StringUtil.getShortNameComponent(qualifiedName);

        Position pos2 = pos.truncateEnd(name.length() + 1);

        return AmbQualifierNode(pos,
                                QualifierNodeFromQualifiedName(pos2, container),
                                name);
    }

    @Override
    public final AmbPrefix AmbPrefix(Position pos, Prefix prefix, String name) {
        return AmbPrefix(pos, prefix, Id(pos, name));
    }

    @Override
    public final AmbReceiver AmbReceiver(Position pos, Prefix prefix,
            String name) {
        return AmbReceiver(pos, prefix, Id(pos, name));
    }

    @Override
    public final AmbQualifierNode AmbQualifierNode(Position pos,
            QualifierNode qualifier, String name) {
        return AmbQualifierNode(pos, qualifier, Id(pos, name));
    }

    @Override
    public final AmbExpr AmbExpr(Position pos, String name) {
        return AmbExpr(pos, Id(pos, name));
    }

    @Override
    public final AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier,
            String name) {
        return AmbTypeNode(pos, qualifier, Id(pos, name));
    }

    @Override
    public final AmbPrefix AmbPrefix(Position pos, Id name) {
        return AmbPrefix(pos, null, name);
    }

    @Override
    public final AmbPrefix AmbPrefix(Position pos, String name) {
        return AmbPrefix(pos, null, name);
    }

    @Override
    public final AmbReceiver AmbReceiver(Position pos, Id name) {
        return AmbReceiver(pos, null, name);
    }

    @Override
    public final AmbReceiver AmbReceiver(Position pos, String name) {
        return AmbReceiver(pos, null, name);
    }

    @Override
    public final AmbQualifierNode AmbQualifierNode(Position pos, Id name) {
        return AmbQualifierNode(pos, null, name);
    }

    @Override
    public final AmbQualifierNode AmbQualifierNode(Position pos, String name) {
        return AmbQualifierNode(pos, null, name);
    }

    @Override
    public final AmbTypeNode AmbTypeNode(Position pos, Id name) {
        return AmbTypeNode(pos, null, name);
    }

    @Override
    public final AmbTypeNode AmbTypeNode(Position pos, String name) {
        return AmbTypeNode(pos, null, name);
    }

    @Override
    public final ArrayInit ArrayInit(Position pos) {
        return ArrayInit(pos, Collections.<Expr> emptyList());
    }

    @Override
    public final Assert Assert(Position pos, Expr cond) {
        return Assert(pos, cond, null);
    }

    @Override
    public final Block Block(Position pos, Stmt... stmts) {
        List<Stmt> l = new ArrayList<Stmt>(1);
        Collections.addAll(l, stmts);
        return Block(pos, l);
    }

    @Override
    public final Branch Branch(Position pos, Branch.Kind kind, String label) {
        return Branch(pos, kind, Id(pos, label));
    }

    @Override
    public final Branch Break(Position pos) {
        return Branch(pos, Branch.BREAK, (Id) null);
    }

    @Override
    public final Branch Break(Position pos, Id label) {
        return Branch(pos, Branch.BREAK, label);
    }

    @Override
    public final Branch Break(Position pos, String label) {
        return Branch(pos, Branch.BREAK, label);
    }

    @Override
    public final Branch Continue(Position pos) {
        return Branch(pos, Branch.CONTINUE, (Id) null);
    }

    @Override
    public final Branch Continue(Position pos, Id label) {
        return Branch(pos, Branch.CONTINUE, label);
    }

    @Override
    public final Branch Continue(Position pos, String label) {
        return Branch(pos, Branch.CONTINUE, label);
    }

    @Override
    public final Branch Branch(Position pos, Branch.Kind kind) {
        return Branch(pos, kind, (Id) null);
    }

    @Override
    public final Call Call(Position pos, Receiver target, String name,
            List<Expr> args) {
        return Call(pos, target, Id(pos, name), args);
    }

    @Override
    public final Call Call(Position pos, Id name, Expr... args) {
        List<Expr> l = new ArrayList<Expr>(1);
        Collections.addAll(l, args);
        return Call(pos, null, name, l);
    }

    @Override
    public final Call Call(Position pos, String name, Expr... args) {
        List<Expr> l = new ArrayList<Expr>(1);
        Collections.addAll(l, args);
        return Call(pos, null, name, l);
    }

    @Override
    public final Call Call(Position pos, Id name, List<Expr> args) {
        return Call(pos, null, name, args);
    }

    @Override
    public final Call Call(Position pos, String name, List<Expr> args) {
        return Call(pos, null, name, args);
    }

    @Override
    public final Call Call(Position pos, Receiver target, Id name, Expr... args) {
        List<Expr> l = new ArrayList<Expr>(1);
        Collections.addAll(l, args);
        return Call(pos, target, name, l);
    }

    @Override
    public final Call Call(Position pos, Receiver target, String name,
            Expr... args) {
        List<Expr> l = new ArrayList<Expr>(1);
        Collections.addAll(l, args);
        return Call(pos, target, name, l);
    }

    @Override
    public final Case Default(Position pos) {
        return Case(pos, null);
    }

    @Override
    public final ClassDecl ClassDecl(Position pos, Flags flags, String name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        return ClassDecl(pos,
                         flags,
                         Id(pos, name),
                         superClass,
                         interfaces,
                         body);
    }

    @Override
    public final ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            String name, List<Formal> formals, List<TypeNode> throwTypes,
            Block body) {
        return ConstructorDecl(pos,
                               flags,
                               Id(pos, name),
                               formals,
                               throwTypes,
                               body);
    }

    @Override
    public final ConstructorCall ThisCall(Position pos, List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.THIS, null, args);
    }

    @Override
    public final ConstructorCall ThisCall(Position pos, Expr outer,
            List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.THIS, outer, args);
    }

    @Override
    public final ConstructorCall SuperCall(Position pos, List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.SUPER, null, args);
    }

    @Override
    public final ConstructorCall SuperCall(Position pos, Expr outer,
            List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.SUPER, outer, args);
    }

    @Override
    public final ConstructorCall ConstructorCall(Position pos,
            ConstructorCall.Kind kind, List<Expr> args) {
        return ConstructorCall(pos, kind, null, args);
    }

    @Override
    public final Field Field(Position pos, Receiver target, String name) {
        return Field(pos, target, Id(pos, name));
    }

    @Override
    public final Formal Formal(Position pos, Flags flags, TypeNode type,
            String name) {
        return Formal(pos, flags, type, Id(pos, name));
    }

    @Override
    public final Local Local(Position pos, String name) {
        return Local(pos, Id(pos, name));
    }

    @Override
    public final LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            String name, Expr init) {
        return LocalDecl(pos, flags, type, Id(pos, name), init);
    }

    @Override
    public final MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, String name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        return MethodDecl(pos,
                          flags,
                          returnType,
                          Id(pos, name),
                          formals,
                          throwTypes,
                          body);
    }

    @Override
    public final Labeled Labeled(Position pos, String label, Stmt body) {
        return Labeled(pos, Id(pos, label), body);
    }

    @Override
    public final FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name) {
        return FieldDecl(pos, flags, type, name, null);
    }

    @Override
    public final FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            String name, Expr init) {
        return FieldDecl(pos, flags, type, Id(pos, name), init);
    }

    @Override
    public final FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            String name) {
        return FieldDecl(pos, flags, type, name, null);
    }

    @Override
    public final Field Field(Position pos, Id name) {
        return Field(pos, null, name);
    }

    @Override
    public final Field Field(Position pos, String name) {
        return Field(pos, null, name);
    }

    @Override
    public final If If(Position pos, Expr cond, Stmt consequent) {
        return If(pos, cond, consequent, null);
    }

    @Override
    public final LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            Id name) {
        return LocalDecl(pos, flags, type, name, null);
    }

    @Override
    public final LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            String name) {
        return LocalDecl(pos, flags, type, name, null);
    }

    @Override
    public final New New(Position pos, TypeNode type, List<Expr> args) {
        return New(pos, null, type, args, null);
    }

    @Override
    public final New New(Position pos, TypeNode type, List<Expr> args,
            ClassBody body) {
        return New(pos, null, type, args, body);
    }

    @Override
    public final New New(Position pos, Expr outer, TypeNode objectType,
            List<Expr> args) {
        return New(pos, outer, objectType, args, null);
    }

    @Override
    public final NewArray NewArray(Position pos, TypeNode base, List<Expr> dims) {
        return NewArray(pos, base, dims, 0, null);
    }

    @Override
    public final NewArray NewArray(Position pos, TypeNode base,
            List<Expr> dims, int addDims) {
        return NewArray(pos, base, dims, addDims, null);
    }

    @Override
    public final NewArray NewArray(Position pos, TypeNode base, int addDims,
            ArrayInit init) {
        return NewArray(pos,
                        base,
                        Collections.<Expr> emptyList(),
                        addDims,
                        init);
    }

    @Override
    public final Return Return(Position pos) {
        return Return(pos, null);
    }

    @Override
    public final SourceFile SourceFile(Position pos, List<TopLevelDecl> decls) {
        return SourceFile(pos, null, Collections.<Import> emptyList(), decls);
    }

    @Override
    public final SourceFile SourceFile(Position pos, List<Import> imports,
            List<TopLevelDecl> decls) {
        return SourceFile(pos, null, imports, decls);
    }

    @Override
    public final Special This(Position pos) {
        return Special(pos, Special.THIS, null);
    }

    @Override
    public final Special This(Position pos, TypeNode outer) {
        return Special(pos, Special.THIS, outer);
    }

    @Override
    public final Special Super(Position pos) {
        return Special(pos, Special.SUPER, null);
    }

    @Override
    public final Special Super(Position pos, TypeNode outer) {
        return Special(pos, Special.SUPER, outer);
    }

    @Override
    public final Special Special(Position pos, Special.Kind kind) {
        return Special(pos, kind, null);
    }

    @Override
    public final Try Try(Position pos, Block tryBlock, List<Catch> catchBlocks) {
        return Try(pos, tryBlock, catchBlocks, null);
    }

    @Override
    public final Unary Unary(Position pos, Expr expr, Unary.Operator op) {
        return Unary(pos, op, expr);
    }
}
