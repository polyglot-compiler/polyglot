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
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Disamb;
import polyglot.ast.Expr;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
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
                new AmbTypeInstantiation(pos, base, typeArguments);
        n =
                (AmbTypeInstantiation) n.ext(extFactory().extAmbTypeInstantiation());
        return n;
    }

    @Override
    public AmbWildCard AmbWildCard(Position pos) {
        AmbWildCard n = new AmbWildCard(pos);
        n = (AmbWildCard) n.ext(extFactory().extAmbWildCard());
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardExtends(Position pos, TypeNode extendsNode) {
        AmbWildCard n = new AmbWildCard(pos, extendsNode, true);
        n = (AmbWildCard) n.ext(extFactory().extAmbWildCard());
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardSuper(Position pos, TypeNode superNode) {
        AmbWildCard n = new AmbWildCard(pos, superNode, false);
        n = (AmbWildCard) n.ext(extFactory().extAmbWildCard());
        return n;
    }

    @Override
    public AnnotationElemDecl AnnotationElemDecl(Position pos, Flags flags,
            TypeNode type, Id name, Term defaultValue) {
        AnnotationElemDecl n =
                new AnnotationElemDecl_c(pos, flags, type, name, defaultValue);
        n = (AnnotationElemDecl) n.ext(extFactory().extAnnotationElemDecl());
        return n;
    }

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
                        null,
                        extFactory());
    }

    protected final ClassDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body, Ext ext,
            ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = ((JL5ExtFactory) extFactory).extEnumDecl();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5EnumDeclExt(CollectionUtil.nonNullList(annotations)));
        return super.ClassDecl(pos,
                               flags,
                               name,
                               superType,
                               interfaces,
                               body,
                               ext,
                               extFactory.nextExtFactory());
    }

    @Override
    public Call Call(Position pos, Receiver target, List<TypeNode> typeArgs,
            Id name, List<Expr> args) {
        return Call(pos, target, typeArgs, name, args, null, extFactory());
    }

    protected final Call Call(Position pos, Receiver target,
            List<TypeNode> typeArgs, Id name, List<Expr> args, Ext ext,
            ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extCall();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5CallExt(CollectionUtil.nonNullList(typeArgs)));
        return super.Call(pos,
                          target,
                          name,
                          args,
                          ext,
                          extFactory.nextExtFactory());
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes) {
        return ClassDecl(pos,
                         flags,
                         annotations,
                         name,
                         superType,
                         interfaces,
                         body,
                         paramTypes,
                         null,
                         extFactory());
    }

    protected final ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extClassDecl();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5ClassDeclExt(CollectionUtil.nonNullList(paramTypes),
                                                CollectionUtil.nonNullList(annotations)));
        return super.ClassDecl(pos,
                               flags,
                               name,
                               superType,
                               interfaces,
                               body,
                               ext,
                               extFactory.nextExtFactory());
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args,
            boolean isEnumConstructorCall) {
        return ConstructorCall(pos,
                               kind,
                               typeArgs,
                               outer,
                               args,
                               isEnumConstructorCall,
                               null,
                               extFactory());
    }

    protected final ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args,
            boolean isEnumConstructorCall, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extConstructorCall();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5ConstructorCallExt(CollectionUtil.nonNullList(typeArgs)));
        return super.ConstructorCall(pos,
                                     kind,
                                     outer,
                                     args,
                                     ext,
                                     extFactory.nextExtFactory());
    }

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
                               null,
                               extFactory());
    }

    protected final ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extConstructorDecl();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5ConstructorDeclExt(CollectionUtil.nonNullList(typeParams),
                                                      CollectionUtil.nonNullList(annotations)));
        return super.ConstructorDecl(pos,
                                     flags,
                                     name,
                                     formals,
                                     throwTypes,
                                     body,
                                     ext,
                                     extFactory.nextExtFactory());
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
        n =
                (ElementValueArrayInit) n.ext(extFactory().extElementValueArrayInit());
        return n;
    }

    @Override
    public ElementValuePair ElementValuePair(Position pos, Id name, Term value) {
        ElementValuePair n = new ElementValuePair_c(pos, name, value);
        n = (ElementValuePair) n.ext(extFactory().extElementValuePair());
        return n;
    }

    @Override
    public EnumConstant EnumConstant(Position pos, Receiver target, Id name) {
        EnumConstant n = new EnumConstant_c(pos, target, name);
        n = (EnumConstant) n.ext(extFactory().extEnumConstant());
        return n;
    }

    @Override
    public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args,
            ClassBody body) {
        return EnumConstantDecl(pos,
                                flags,
                                annotations,
                                name,
                                args,
                                body,
                                null,
                                extFactory());
    }

    protected final EnumConstantDecl EnumConstantDecl(Position pos,
            Flags flags, List<AnnotationElem> annotations, Id name,
            List<Expr> args, ClassBody body, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = ((JL5ExtFactory) extFactory).extEnumConstantDecl();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new EnumConstantDeclExt(CollectionUtil.nonNullList(annotations)));
        extFactory = extFactory.nextExtFactory();
        //TODO
        return new EnumConstantDecl_c(pos, flags, name, args, body, ext);
    }

    @Override
    public ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr,
            Stmt stmt) {
        ExtendedFor n = new ExtendedFor_c(pos, decl, expr, stmt);
        n = (ExtendedFor) n.ext(extFactory().extExtendedFor());
        return n;
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        return FieldDecl(pos,
                         flags,
                         annotations,
                         type,
                         name,
                         init,
                         null,
                         extFactory());
    }

    protected final FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            Expr init, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extFieldDecl();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5FieldDeclExt(CollectionUtil.nonNullList(annotations)));
        return super.FieldDecl(pos,
                               flags,
                               type,
                               name,
                               init,
                               ext,
                               extFactory.nextExtFactory());
    }

    @Override
    public Formal Formal(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            boolean var_args) {
        return Formal(pos,
                      flags,
                      annotations,
                      type,
                      name,
                      var_args,
                      null,
                      extFactory());
    }

    protected Formal Formal(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            boolean isVarArg, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extFormal();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5FormalExt(isVarArg,
                                             CollectionUtil.nonNullList(annotations)));
        return super.Formal(pos,
                            flags,
                            type,
                            name,
                            ext,
                            extFactory.nextExtFactory());
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        return LocalDecl(pos,
                         flags,
                         annotations,
                         type,
                         name,
                         init,
                         null,
                         extFactory());
    }

    protected final LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            Expr init, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extLocalDecl();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5LocalDeclExt(CollectionUtil.nonNullList(annotations)));
        return super.LocalDecl(pos,
                               flags,
                               type,
                               name,
                               init,
                               ext,
                               extFactory.nextExtFactory());
    }

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
                          null,
                          extFactory());
    }

    protected final MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extMethodDecl();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5MethodDeclExt(CollectionUtil.nonNullList(typeParams),
                                                 CollectionUtil.nonNullList(annotations)));
        return super.MethodDecl(pos,
                                flags,
                                returnType,
                                name,
                                formals,
                                throwTypes,
                                body,
                                ext,
                                extFactory.nextExtFactory());
    }

    @Override
    public New New(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body) {
        return New(pos,
                   outer,
                   typeArgs,
                   objectType,
                   args,
                   body,
                   null,
                   extFactory());
    }

    protected final New New(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body, Ext ext,
            ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = extFactory.extNew();
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext =
                composeExts(ext,
                            new JL5NewExt(CollectionUtil.nonNullList(typeArgs)));
        return super.New(pos,
                         outer,
                         objectType,
                         args,
                         body,
                         ext,
                         extFactory.nextExtFactory());
    }

    @Override
    public AnnotationElem NormalAnnotationElem(Position pos, TypeNode name,
            List<ElementValuePair> elements) {
        AnnotationElem n =
                new AnnotationElem_c(pos,
                                     name,
                                     CollectionUtil.nonNullList(elements));
        n = (AnnotationElem) n.ext(extFactory().extNormalAnnotationElem());
        return n;
    }

    @Override
    public ParamTypeNode ParamTypeNode(Position pos, List<TypeNode> bounds,
            Id id) {
        ParamTypeNode n = new ParamTypeNode_c(pos, bounds, id);
        n = (ParamTypeNode) n.ext(extFactory().extParamTypeNode());
        return n;
    }

    @Override
    public AnnotationElem SingleElementAnnotationElem(Position pos,
            TypeNode name, Term value) {
        List<ElementValuePair> l = new LinkedList<ElementValuePair>();
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
