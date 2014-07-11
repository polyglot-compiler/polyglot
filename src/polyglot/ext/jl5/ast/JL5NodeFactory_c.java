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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Disamb;
import polyglot.ast.Expr;
import polyglot.ast.ExtFactory;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.JLAbstractExtFactory_c;
import polyglot.ast.JLang_c;
import polyglot.ast.Lang;
import polyglot.ast.LocalDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.New;
import polyglot.ast.NodeOps;
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
    public J5Lang lang() {
        return (J5Lang) super.lang();
    }

    @Override
    public AmbTypeInstantiation AmbTypeInstantiation(Position pos,
            TypeNode base, List<TypeNode> typeArguments) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbTypeInstantiation(pos,
                                    base,
                                    typeArguments,
                                    J5Lang_c.instance,
                                    nodeMap,
                                    extFactory());
    }

    protected final AmbTypeInstantiation AmbTypeInstantiation(Position pos,
            TypeNode base, List<TypeNode> typeArguments, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang,
                        ((JL5ExtFactory) extFactory).extAmbTypeInstantiation());
        }
        AmbTypeInstantiation n =
                new AmbTypeInstantiation(pos,
                                         base,
                                         CollectionUtil.nonNullList(typeArguments));
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardExtends(Position pos, TypeNode extendsNode) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbWildCard(pos,
                           extendsNode,
                           true,
                           J5Lang_c.instance,
                           nodeMap,
                           extFactory());
    }

    @Override
    public AmbWildCard AmbWildCardSuper(Position pos, TypeNode superNode) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbWildCard(pos,
                           superNode,
                           false,
                           J5Lang_c.instance,
                           nodeMap,
                           extFactory());
    }

    protected final AmbWildCard AmbWildCard(Position pos, TypeNode constraint,
            boolean isExtendsConstraint, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, ((JL5ExtFactory) extFactory).extAmbWildCard());
        }
        AmbWildCard n = new AmbWildCard(pos, constraint, isExtendsConstraint);
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public AnnotationElemDecl AnnotationElemDecl(Position pos, Flags flags,
            TypeNode type, Id name, Term defaultValue) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AnnotationElemDecl(pos,
                                  flags,
                                  type,
                                  name,
                                  defaultValue,
                                  J5Lang_c.instance,
                                  nodeMap,
                                  extFactory());
    }

    protected final AnnotationElemDecl AnnotationElemDecl(Position pos,
            Flags flags, TypeNode type, Id name, Term defaultValue,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang,
                        ((JL5ExtFactory) extFactory).extAnnotationElemDecl());
        }
        AnnotationElemDecl n =
                new AnnotationElemDecl_c(pos, flags, type, name, defaultValue);
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public ClassDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        // TODO actually J5Lang
        return EnumDecl(pos,
                        flags,
                        annotations,
                        name,
                        superClass,
                        interfaces,
                        body,
                        JLang_c.instance,
                        nodeMap,
                        extFactory());
    }

    protected final ClassDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, ((JL5ExtFactory) extFactory).extEnumDecl());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5EnumDeclExt(CollectionUtil.nonNullList(annotations)));
        return super.ClassDecl(pos,
                               flags,
                               name,
                               superClass,
                               interfaces,
                               body,
                               primaryLang,
                               nodeMap,
                               extFactory.nextExtFactory());
    }

    @Override
    public Call Call(Position pos, Receiver target, List<TypeNode> typeArgs,
            Id name, List<Expr> args) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Call(pos,
                    target,
                    typeArgs,
                    name,
                    args,
                    JLang_c.instance,
                    nodeMap,
                    extFactory());
    }

    protected final Call Call(Position pos, Receiver target,
            List<TypeNode> typeArgs, Id name, List<Expr> args,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extCall());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5CallExt(CollectionUtil.nonNullList(typeArgs)));
        return super.Call(pos,
                          target,
                          name,
                          args,
                          primaryLang,
                          nodeMap,
                          extFactory.nextExtFactory());
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ClassDecl(pos,
                         flags,
                         annotations,
                         name,
                         superClass,
                         interfaces,
                         body,
                         paramTypes,
                         JLang_c.instance,
                         nodeMap,
                         extFactory());
    }

    protected final ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extClassDecl());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5ClassDeclExt(CollectionUtil.nonNullList(paramTypes),
                                        CollectionUtil.nonNullList(annotations)));
        return super.ClassDecl(pos,
                               flags,
                               name,
                               superClass,
                               interfaces,
                               body,
                               primaryLang,
                               nodeMap,
                               extFactory.nextExtFactory());
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args,
            boolean isEnumConstructorCall) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ConstructorCall(pos,
                               kind,
                               typeArgs,
                               outer,
                               args,
                               isEnumConstructorCall,
                               JLang_c.instance,
                               nodeMap,
                               extFactory());
    }

    protected final ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args,
            boolean isEnumConstructorCall, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extConstructorCall());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5ConstructorCallExt(CollectionUtil.nonNullList(typeArgs),
                                              isEnumConstructorCall));
        return super.ConstructorCall(pos,
                                     kind,
                                     outer,
                                     args,
                                     primaryLang,
                                     nodeMap,
                                     extFactory.nextExtFactory());
    }

    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ConstructorDecl(pos,
                               flags,
                               annotations,
                               name,
                               formals,
                               throwTypes,
                               body,
                               typeParams,
                               JLang_c.instance,
                               nodeMap,
                               extFactory());
    }

    protected final ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extConstructorDecl());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5ConstructorDeclExt(CollectionUtil.nonNullList(typeParams),
                                              CollectionUtil.nonNullList(annotations)));
        return super.ConstructorDecl(pos,
                                     flags,
                                     name,
                                     formals,
                                     throwTypes,
                                     body,
                                     primaryLang,
                                     nodeMap,
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
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ElementValueArrayInit(pos,
                                     elements,
                                     J5Lang_c.instance,
                                     nodeMap,
                                     extFactory());
    }

    protected final ElementValueArrayInit ElementValueArrayInit(Position pos,
            List<Term> elements, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang,
                        ((JL5ExtFactory) extFactory).extElementValueArrayInit());
        }
        ElementValueArrayInit n = new ElementValueArrayInit_c(pos, elements);
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public ElementValuePair ElementValuePair(Position pos, Id name, Term value) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ElementValuePair(pos,
                                name,
                                value,
                                J5Lang_c.instance,
                                nodeMap,
                                extFactory());
    }

    protected final ElementValuePair ElementValuePair(Position pos, Id name,
            Term value, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang,
                        ((JL5ExtFactory) extFactory).extElementValuePair());
        }
        ElementValuePair n = new ElementValuePair_c(pos, name, value);
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public EnumConstant EnumConstant(Position pos, Receiver target, Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return EnumConstant(pos,
                            target,
                            name,
                            J5Lang_c.instance,
                            nodeMap,
                            extFactory());
    }

    protected final EnumConstant EnumConstant(Position pos, Receiver target,
            Id name, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, ((JL5ExtFactory) extFactory).extEnumConstant());
        }
        EnumConstant n = new EnumConstant_c(pos, target, name);
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args,
            ClassBody body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        // TODO actually J5Lang
        return EnumConstantDecl(pos,
                                flags,
                                annotations,
                                name,
                                args,
                                body,
                                JLang_c.instance,
                                nodeMap,
                                extFactory());
    }

    protected final EnumConstantDecl EnumConstantDecl(Position pos,
            Flags flags, List<AnnotationElem> annotations, Id name,
            List<Expr> args, ClassBody body, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang,
                        ((JL5ExtFactory) extFactory).extEnumConstantDecl());
        }
        // TODO
        nodeMap.put(J5Lang_c.instance,
                    new EnumConstantDeclExt(CollectionUtil.nonNullList(annotations)));
        EnumConstantDecl n =
                new EnumConstantDecl_c(pos, flags, name, args, body);
        nodeMap.put(JLang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr,
            Stmt body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ExtendedFor(pos,
                           decl,
                           expr,
                           body,
                           J5Lang_c.instance,
                           nodeMap,
                           extFactory());
    }

    protected final ExtendedFor ExtendedFor(Position pos, LocalDecl decl,
            Expr expr, Stmt body, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, ((JL5ExtFactory) extFactory).extExtendedFor());
        }
        ExtendedFor n = new ExtendedFor_c(pos, decl, expr, body);
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return FieldDecl(pos,
                         flags,
                         annotations,
                         type,
                         name,
                         init,
                         JLang_c.instance,
                         nodeMap,
                         extFactory());
    }

    protected final FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            Expr init, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extFieldDecl());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5FieldDeclExt(CollectionUtil.nonNullList(annotations)));
        return super.FieldDecl(pos,
                               flags,
                               type,
                               name,
                               init,
                               primaryLang,
                               nodeMap,
                               extFactory.nextExtFactory());
    }

    @Override
    public Formal Formal(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            boolean isVarArg) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Formal(pos,
                      flags,
                      annotations,
                      type,
                      name,
                      isVarArg,
                      JLang_c.instance,
                      nodeMap,
                      extFactory());
    }

    protected final Formal Formal(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            boolean isVarArg, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extFormal());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5FormalExt(isVarArg,
                                     CollectionUtil.nonNullList(annotations)));
        return super.Formal(pos,
                            flags,
                            type,
                            name,
                            primaryLang,
                            nodeMap,
                            extFactory.nextExtFactory());
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return LocalDecl(pos,
                         flags,
                         annotations,
                         type,
                         name,
                         init,
                         JLang_c.instance,
                         nodeMap,
                         extFactory());
    }

    protected final LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            Expr init, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extLocalDecl());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5LocalDeclExt(CollectionUtil.nonNullList(annotations)));
        return super.LocalDecl(pos,
                               flags,
                               type,
                               name,
                               init,
                               primaryLang,
                               nodeMap,
                               extFactory.nextExtFactory());
    }

    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return MethodDecl(pos,
                          flags,
                          annotations,
                          returnType,
                          name,
                          formals,
                          throwTypes,
                          body,
                          typeParams,
                          JLang_c.instance,
                          nodeMap,
                          extFactory());
    }

    protected final MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extMethodDecl());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5MethodDeclExt(CollectionUtil.nonNullList(typeParams),
                                         CollectionUtil.nonNullList(annotations)));
        return super.MethodDecl(pos,
                                flags,
                                returnType,
                                name,
                                formals,
                                throwTypes,
                                body,
                                primaryLang,
                                nodeMap,
                                extFactory.nextExtFactory());
    }

    @Override
    public New New(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return New(pos,
                   outer,
                   typeArgs,
                   objectType,
                   args,
                   body,
                   JLang_c.instance,
                   nodeMap,
                   extFactory());
    }

    protected final New New(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, extFactory.extNew());
        }
        nodeMap.put(J5Lang_c.instance,
                    new JL5NewExt(CollectionUtil.nonNullList(typeArgs)));
        return super.New(pos,
                         outer,
                         objectType,
                         args,
                         body,
                         primaryLang,
                         nodeMap,
                         extFactory.nextExtFactory());
    }

    @Override
    public AnnotationElem NormalAnnotationElem(Position pos, TypeNode name,
            List<ElementValuePair> elements) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return NormalAnnotationElem(pos,
                                    name,
                                    elements,
                                    J5Lang_c.instance,
                                    nodeMap,
                                    extFactory());
    }

    protected final AnnotationElem NormalAnnotationElem(Position pos,
            TypeNode name, List<ElementValuePair> elements, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang,
                        ((JL5ExtFactory) extFactory).extNormalAnnotationElem());
        }
        AnnotationElem n =
                new AnnotationElem_c(pos,
                                     name,
                                     CollectionUtil.nonNullList(elements));
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
        return n;
    }

    @Override
    public ParamTypeNode ParamTypeNode(Position pos, Id id,
            List<TypeNode> bounds) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ParamTypeNode(pos,
                             id,
                             bounds,
                             J5Lang_c.instance,
                             nodeMap,
                             extFactory());
    }

    protected final ParamTypeNode ParamTypeNode(Position pos, Id id,
            List<TypeNode> bounds, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory()) {
            Lang lang = extFactory.lang();
            if (lang == J5Lang_c.instance) break;
            nodeMap.put(lang, ((JL5ExtFactory) extFactory).extParamTypeNode());
        }
        ParamTypeNode n =
                new ParamTypeNode_c(pos, id, CollectionUtil.nonNullList(bounds));
        nodeMap.put(J5Lang_c.instance, n);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
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
