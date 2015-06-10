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

import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassDecl_c;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Disamb;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Javadoc;
import polyglot.ast.LocalDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.New;
import polyglot.ast.Receiver;
import polyglot.ast.Stmt;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;

/**
 * NodeFactory for jl5 extension.
 */
public class JL5NodeFactory_c extends JL5AbstractNodeFactory_c {
    public JL5NodeFactory_c() {
        this(J5Lang_c.instance);
    }

    public JL5NodeFactory_c(J5Lang lang) {
        super(lang);
    }

    public JL5NodeFactory_c(J5Lang lang, JL5ExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public JL5ExtFactory extFactory() {
        return (JL5ExtFactory) super.extFactory();
    }

    @Override
    public J5Lang lang() {
        return (J5Lang) super.lang();
    }

    @Override
    public AmbTypeInstantiation AmbTypeInstantiation(Position pos,
            TypeNode base, List<TypeNode> typeArguments) {
        AmbTypeInstantiation n =
                new AmbTypeInstantiation(pos,
                                         base,
                                         CollectionUtil.nonNullList(typeArguments));
        n = ext(n, extFactory().extAmbTypeInstantiation());
        return n;
    }

    @Override
    public AmbWildCard AmbWildCard(Position pos) {
        AmbWildCard n = new AmbWildCard(pos);
        n = ext(n, extFactory().extAmbWildCard());
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardExtends(Position pos, TypeNode extendsNode) {
        AmbWildCard n = new AmbWildCard(pos, extendsNode, true);
        n = ext(n, extFactory().extAmbWildCard());
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardSuper(Position pos, TypeNode superNode) {
        AmbWildCard n = new AmbWildCard(pos, superNode, false);
        n = ext(n, extFactory().extAmbWildCard());
        return n;
    }

    @Override
    public AnnotationElemDecl AnnotationElemDecl(Position pos, Flags flags,
            TypeNode type, Id name, Term defaultValue, Javadoc javadoc) {
        AnnotationElemDecl n =
                new AnnotationElemDecl_c(pos,
                                         flags,
                                         type,
                                         name,
                                         defaultValue,
                                         javadoc);
        n = ext(n, extFactory().extAnnotationElemDecl());
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public AnnotationElemDecl AnnotationElemDecl(Position pos, Flags flags,
            TypeNode type, Id name, Term defaultValue) {
        return AnnotationElemDecl(pos, flags, type, name, defaultValue, null);
    }

    @Override
    public ClassDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body, Javadoc javadoc) {
        ClassDecl n =
                new ClassDecl_c(pos, flags, name, superType, interfaces, body, javadoc);
        n = ext(n, extFactory().extEnumDecl());
        JL5EnumDeclExt ext = (JL5EnumDeclExt) JL5Ext.ext(n);
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public ClassDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body) {
        return EnumDecl(pos,
                        flags,
                        annotations,
                        name,
                        superType,
                        interfaces,
                        body,
                        null);
    }

    @Override
    public final Call Call(Position pos, Receiver target, Id name,
            List<Expr> args) {
        return Call(pos, target, null, name, args);
    }

    @Override
    public Call Call(Position pos, Receiver target, List<TypeNode> typeArgs,
            Id name, List<Expr> args) {
        Call n =
                super.Call(pos, target, name, CollectionUtil.nonNullList(args));
        JL5CallExt ext = (JL5CallExt) JL5Ext.ext(n);
        ext.typeArgs = CollectionUtil.nonNullList(typeArgs);
        return n;
    }

    @Override
    public final ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body,
            Javadoc javadoc) {
        return ClassDecl(pos,
                         flags,
                         null,
                         name,
                         superClass,
                         interfaces,
                         body,
                         null,
                         javadoc);
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public final ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        return ClassDecl(pos,
                         flags,
                         null,
                         name,
                         superClass,
                         interfaces,
                         body,
                         null,
                         null);
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes, Javadoc javadoc) {

        ClassDecl n =
                super.ClassDecl(pos,
                                flags,
                                name,
                                superType,
                                interfaces,
                                body,
                                javadoc);
        JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(n);
        ext.paramTypes = CollectionUtil.nonNullList(paramTypes);
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes) {
        ClassDecl n =
                super.ClassDecl(pos, flags, name, superType, interfaces, body);
        JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(n);
        ext.paramTypes = CollectionUtil.nonNullList(paramTypes);
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return n;
    }

    @Override
    public final ConstructorCall ConstructorCall(Position pos, Kind kind,
            Expr outer, List<Expr> args) {
        return ConstructorCall(pos, kind, outer, args, false);
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args,
            boolean isEnumConstructorCall) {
        ConstructorCall n = super.ConstructorCall(pos, kind, outer, args);
        JL5ConstructorCallExt ext = (JL5ConstructorCallExt) JL5Ext.ext(n);
        ext.typeArgs = CollectionUtil.nonNullList(typeArgs);
        ext.isEnumConstructorCall = isEnumConstructorCall;
        return n;
    }

    @Override
    public final ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            Id name, List<Formal> formals, List<TypeNode> throwTypes,
            Block body, Javadoc javadoc) {
        ConstructorDecl n =
                ConstructorDecl(pos,
                                flags,
                                null,
                                name,
                                formals,
                                throwTypes,
                                body,
                                null,
                                javadoc);
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public final ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            Id name, List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        ConstructorDecl n =
                ConstructorDecl(pos,
                                flags,
                                null,
                                name,
                                formals,
                                throwTypes,
                                body,
                                null);
        return n;
    }

    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams, Javadoc javadoc) {
        ConstructorDecl n =
                super.ConstructorDecl(pos,
                                      flags,
                                      name,
                                      formals,
                                      throwTypes,
                                      body,
                                      javadoc);
        JL5ConstructorDeclExt ext = (JL5ConstructorDeclExt) JL5Ext.ext(n);
        ext.typeParams = CollectionUtil.nonNullList(typeParams);
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams) {
        return ConstructorDecl(pos,
                               flags,
                               annotations,
                               name,
                               formals,
                               throwTypes,
                               body,
                               typeParams,
                               null);
    }

    @Override
    public Disamb disamb() {
        Disamb n = new JL5Disamb_c();
        return n;
    }

    @Override
    public ElementValueArrayInit ElementValueArrayInit(Position pos,
            List<Term> elements) {
        ElementValueArrayInit n = new ElementValueArrayInit_c(pos, elements);
        n = ext(n, extFactory().extElementValueArrayInit());
        return n;
    }

    @Override
    public ElementValuePair ElementValuePair(Position pos, Id name, Term value) {
        ElementValuePair n = new ElementValuePair_c(pos, name, value);
        n = ext(n, extFactory().extElementValuePair());
        return n;
    }

    @Override
    public EnumConstant EnumConstant(Position pos, Receiver target, Id name) {
        EnumConstant n = new EnumConstant_c(pos, target, name);
        n = ext(n, extFactory().extEnumConstant());
        return n;
    }

    @Override
    public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args,
            ClassBody body, Javadoc javadoc) {
        EnumConstantDecl n =
                new EnumConstantDecl_c(pos, flags, name, args, body, javadoc);
        n = ext(n, extFactory().extEnumConstantDecl());
        EnumConstantDeclExt ext = (EnumConstantDeclExt) JL5Ext.ext(n);
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args,
            ClassBody body) {
        return EnumConstantDecl(pos, flags, annotations, name, args, body, null);
    }

    @Override
    public ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr,
            Stmt body) {
        ExtendedFor n = new ExtendedFor_c(pos, decl, expr, body);
        n = ext(n, extFactory().extExtendedFor());
        return n;
    }

    @Override
    public final FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init, Javadoc javadoc) {
        return FieldDecl(pos, flags, null, type, name, init, javadoc);
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public final FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        return FieldDecl(pos, flags, null, type, name, init);
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            Expr init, Javadoc javadoc) {
        FieldDecl n = super.FieldDecl(pos, flags, type, name, init, javadoc);
        JL5FieldDeclExt ext = (JL5FieldDeclExt) JL5Ext.ext(n);
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        return FieldDecl(pos, flags, annotations, type, name, init, null);
    }

    @Override
    public final Formal Formal(Position pos, Flags flags, TypeNode type, Id name) {
        return Formal(pos, flags, null, type, name);
    }

    @Override
    public Formal Formal(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            boolean var_args) {
        Formal f = super.Formal(pos, flags, type, name);
        JL5FormalExt ext = (JL5FormalExt) JL5Ext.ext(f);
        ext.isVarArg = var_args;
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return f;
    }

    @Override
    public final LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        return LocalDecl(pos, flags, null, type, name, init);
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        LocalDecl n = super.LocalDecl(pos, flags, type, name, init);
        JL5LocalDeclExt ext = (JL5LocalDeclExt) JL5Ext.ext(n);
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return n;
    }

    @Override
    public final MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body, Javadoc javadoc) {
        MethodDecl n =
                MethodDecl(pos,
                           flags,
                           null,
                           returnType,
                           name,
                           formals,
                           throwTypes,
                           body,
                           null,
                           javadoc);
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public final MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        MethodDecl n =
                MethodDecl(pos,
                           flags,
                           null,
                           returnType,
                           name,
                           formals,
                           throwTypes,
                           body,
                           null);
        return n;
    }

    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams, Javadoc javadoc) {
        MethodDecl n =
                super.MethodDecl(pos,
                                 flags,
                                 returnType,
                                 name,
                                 formals,
                                 throwTypes,
                                 body,
                                 javadoc);
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(n);
        ext.typeParams = CollectionUtil.nonNullList(typeParams);
        ext.annotations = CollectionUtil.nonNullList(annotations);
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams) {
        return MethodDecl(pos,
                          flags,
                          annotations,
                          returnType,
                          name,
                          formals,
                          throwTypes,
                          body,
                          typeParams,
                          null);
    }

    @Override
    public final New New(Position pos, Expr outer, TypeNode objectType,
            List<Expr> args, ClassBody body) {
        return New(pos, outer, null, objectType, args, body);
    }

    @Override
    public New New(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body) {
        New n = super.New(pos, outer, objectType, args, body);
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(n);
        ext.typeArgs = CollectionUtil.nonNullList(typeArgs);
        return n;
    }

    @Override
    public AnnotationElem NormalAnnotationElem(Position pos, TypeNode name,
            List<ElementValuePair> elements) {
        AnnotationElem n =
                new AnnotationElem_c(pos,
                                     name,
                                     CollectionUtil.nonNullList(elements));
        n = ext(n, extFactory().extNormalAnnotationElem());
        return n;
    }

    @Override
    public ParamTypeNode ParamTypeNode(Position pos, Id id,
            List<TypeNode> bounds) {
        ParamTypeNode n =
                new ParamTypeNode_c(pos, id, CollectionUtil.nonNullList(bounds));
        n = ext(n, extFactory().extParamTypeNode());
        return n;
    }

    @Override
    public AnnotationElem SingleElementAnnotationElem(Position pos,
            TypeNode name, Term value) {
        List<ElementValuePair> l = new LinkedList<>();
        l.add(ElementValuePair(pos, this.Id(pos, "value"), value));
        return NormalAnnotationElem(pos, name, l);
    }

    @Override
    public TypeNode TypeNodeFromQualifiedName(Position pos,
            String qualifiedName, List<TypeNode> typeArguments) {
        TypeNode base = super.TypeNodeFromQualifiedName(pos, qualifiedName);
        if (typeArguments.isEmpty())
            return base;
        else return AmbTypeInstantiation(pos, base, typeArguments);
    }
}
