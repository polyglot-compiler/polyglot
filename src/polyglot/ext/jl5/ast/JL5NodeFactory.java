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
package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Javadoc;
import polyglot.ast.LocalDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.New;
import polyglot.ast.NodeFactory;
import polyglot.ast.Receiver;
import polyglot.ast.Stmt;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;

/**
 * NodeFactory for jl5 extension.
 */
public interface JL5NodeFactory extends NodeFactory {
    ClassDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body, Javadoc javadoc);

    /** @deprecated */
    @Deprecated
    ClassDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body);

    EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args,
            ClassBody body, Javadoc javadoc);

    /** @deprecated */
    @Deprecated
    EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args,
            ClassBody body);

    EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, Javadoc javadoc,
            List<Expr> args);

    /** @deprecated */
    @Deprecated
    EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args);

    /** @deprecated */
    @Deprecated
    ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes);

    ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes, Javadoc javadoc);

    /** @deprecated */
    @Deprecated
    ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams);

    ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams, Javadoc javadoc);

    /** @deprecated */
    @Deprecated
    MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams);

    MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams, Javadoc javadoc);

    Formal Formal(Position pos, Flags flags, List<AnnotationElem> annotations,
            TypeNode type, Id name, boolean var_args);

    Formal Formal(Position pos, Flags flags, List<AnnotationElem> annotations,
            TypeNode type, Id name);

    LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name);

    LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init);

    FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name);

    /** @deprecated */
    @Deprecated
    FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init);

    FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            Expr init, Javadoc javadoc);

    EnumConstant EnumConstant(Position pos, Receiver r, Id name);

    ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr, Stmt body);

    ParamTypeNode ParamTypeNode(Position pos, Id id, List<TypeNode> bounds);

    AmbTypeInstantiation AmbTypeInstantiation(Position pos, TypeNode base,
            List<TypeNode> typeArguments);

    AmbWildCard AmbWildCard(Position pos);

    AmbWildCard AmbWildCardExtends(Position pos, TypeNode extendsNode);

    AmbWildCard AmbWildCardSuper(Position pos, TypeNode superNode);

    Call Call(Position pos, Receiver target, List<TypeNode> typeArgs, Id name,
            List<Expr> args);

    New New(Position pos, List<TypeNode> typeArgs, TypeNode type,
            List<Expr> args, ClassBody body);

    New New(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body);

    ConstructorCall ConstructorCall(Position pos, Kind kind, Expr outer,
            List<Expr> args, boolean isEnumSuperCall);

    ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args,
            boolean isEnumSuperCall);

    ConstructorCall ThisCall(Position pos, List<TypeNode> typeArgs,
            List<Expr> args);

    ConstructorCall ThisCall(Position pos, List<TypeNode> typeArgs, Expr outer,
            List<Expr> args);

    ConstructorCall SuperCall(Position pos, List<TypeNode> typeArgs,
            List<Expr> args);

    ConstructorCall SuperCall(Position pos, List<TypeNode> typeArgs,
            Expr outer, List<Expr> args);

    ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind,
            List<TypeNode> typeArgs, List<Expr> args);

    ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args);

    AnnotationElemDecl AnnotationElemDecl(Position pos, Flags flags,
            TypeNode type, Id name, Term def, Javadoc javadoc);

    /** @deprecated */
    @Deprecated
    AnnotationElemDecl AnnotationElemDecl(Position pos, Flags flags,
            TypeNode type, Id name, Term def);

    AnnotationElem NormalAnnotationElem(Position pos, TypeNode name,
            List<ElementValuePair> elements);

    AnnotationElem MarkerAnnotationElem(Position pos, TypeNode name);

    AnnotationElem SingleElementAnnotationElem(Position pos, TypeNode name,
            Term value);

    ElementValuePair ElementValuePair(Position pos, Id name, Term value);

    ElementValueArrayInit ElementValueArrayInit(Position pos);

    ElementValueArrayInit ElementValueArrayInit(Position pos,
            List<Term> elements);

    TypeNode TypeNodeFromQualifiedName(Position pos, String qualifiedName,
            List<TypeNode> typeArgs);

}
