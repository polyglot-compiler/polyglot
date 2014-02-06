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

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public abstract class JL5AbstractExtFactory_c extends AbstractExtFactory_c
        implements JL5ExtFactory {

    public JL5AbstractExtFactory_c() {
        super();
    }

    public JL5AbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    public final Ext extAmbTypeInstantiation() {
        Ext e = extAmbTypeInstantiationImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extAmbTypeInstantiation(extFactory));
        return postExtAmbTypeInstantiation(e);
    }

    @Override
    public final Ext extAmbWildCard() {
        Ext e = extAmbWildCardImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extAmbWildCard(extFactory));
        return postExtAmbWildCard(e);
    }

    @Override
    public final Ext extEnumDecl() {
        Ext e = extEnumDeclImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extEnumDecl(extFactory));
        return postExtEnumDecl(e);
    }

    @Override
    public final Ext extExtendedFor() {
        Ext e = extExtendedForImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extExtendedFor(extFactory));
        return postExtExtendedFor(e);
    }

    @Override
    public final Ext extEnumConstantDecl() {
        Ext e = extEnumConstantDeclImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extEnumConstantDecl(extFactory));
        return postExtEnumConstantDecl(e);
    }

    @Override
    public final Ext extEnumConstant() {
        Ext e = extEnumConstantImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extEnumConstant(extFactory));
        return postExtEnumConstant(e);
    }

    @Override
    public final Ext extParamTypeNode() {
        Ext e = extParamTypeNodeImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extParamTypeNode(extFactory));
        return postExtParamTypeNode(e);
    }

    @Override
    public final Ext extAnnotationElemDecl() {
        Ext e = extAnnotationElemDeclImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extAnnotationElemDecl(extFactory));
        return postExtAnnotationElemDecl(e);
    }

    @Override
    public final Ext extNormalAnnotationElem() {
        Ext e = extNormalAnnotationElemImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extNormalAnnotationElem(extFactory));
        return postExtNormalAnnotationElem(e);
    }

    @Override
    public final Ext extMarkerAnnotationElem() {
        Ext e = extMarkerAnnotationElemImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extMarkerAnnotationElem(extFactory));
        return postExtMarkerAnnotationElem(e);
    }

    @Override
    public final Ext extSingleElementAnnotationElem() {
        Ext e = extSingleElementAnnotationElemImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extSingleElementAnnotationElem(extFactory));
        return postExtSingleElementAnnotationElem(e);
    }

    @Override
    public final Ext extElementValuePair() {
        Ext e = extElementValuePairImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extElementValuePair(extFactory));
        return postExtElementValuePair(e);
    }

    @Override
    public final Ext extElementValueArrayInit() {
        Ext e = extElementValueArrayInitImpl();
        for (ExtFactory extFactory = nextExtFactory(); extFactory != null; extFactory =
                extFactory.nextExtFactory())
            e = composeExts(e, extElementValueArrayInit(extFactory));
        return postExtElementValueArrayInit(e);
    }

    protected static final Ext extAmbTypeInstantiation(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extAmbTypeInstantiationImpl();
        return extTypeNode(extFactory);
    }

    protected static final Ext extAmbWildCard(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extAmbWildCardImpl();
        return extTypeNode(extFactory);
    }

    protected static final Ext extEnumDecl(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extEnumDeclImpl();
        return extClassDecl(extFactory);
    }

    protected static final Ext extExtendedFor(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extExtendedForImpl();
        return extLoop(extFactory);
    }

    protected static final Ext extEnumConstantDecl(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extEnumConstantDeclImpl();
        return extClassMember(extFactory);
    }

    protected static final Ext extEnumConstant(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extEnumConstantImpl();
        return extField(extFactory);
    }

    protected static final Ext extParamTypeNode(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extParamTypeNodeImpl();
        return extTypeNode(extFactory);
    }

    protected static final Ext extAnnotationElemDecl(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extAnnotationElemDeclImpl();
        return extMethodDecl(extFactory);
    }

    protected static final Ext extNormalAnnotationElem(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extNormalAnnotationElemImpl();
        return extTerm(extFactory);
    }

    protected static final Ext extMarkerAnnotationElem(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extMarkerAnnotationElemImpl();
        return extTerm(extFactory);
    }

    protected static final Ext extSingleElementAnnotationElem(
            ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extSingleElementAnnotationElemImpl();
        return extTerm(extFactory);
    }

    protected static final Ext extElementValuePair(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extElementValuePairImpl();
        return extTerm(extFactory);
    }

    protected static final Ext extElementValueArrayInit(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5AbstractExtFactory_c) extFactory).extElementValueArrayInitImpl();
        return extTerm(extFactory);
    }

    protected Ext extAmbTypeInstantiationImpl() {
        return this.extTypeNodeImpl();
    }

    protected Ext extAmbWildCardImpl() {
        return this.extTypeNodeImpl();
    }

    protected Ext extEnumDeclImpl() {
        return this.extClassDeclImpl();
    }

    protected Ext extExtendedForImpl() {
        return this.extLoopImpl();
    }

    protected Ext extEnumConstantDeclImpl() {
        return this.extTermImpl();
    }

    protected Ext extEnumConstantImpl() {
        return this.extFieldImpl();
    }

    protected Ext extParamTypeNodeImpl() {
        return this.extTypeNodeImpl();
    }

    protected Ext extAnnotationElemDeclImpl() {
        return this.extClassMemberImpl();
    }

    protected Ext extNormalAnnotationElemImpl() {
        return this.extTermImpl();
    }

    protected Ext extMarkerAnnotationElemImpl() {
        return this.extNormalAnnotationElemImpl();
    }

    protected Ext extSingleElementAnnotationElemImpl() {
        return this.extNormalAnnotationElemImpl();
    }

    protected Ext extElementValuePairImpl() {
        return this.extTermImpl();
    }

    protected Ext extElementValueArrayInitImpl() {
        return this.extTermImpl();
    }

    protected Ext postExtAmbTypeInstantiation(Ext ext) {
        return this.postExtTypeNode(ext);
    }

    protected Ext postExtAmbWildCard(Ext ext) {
        return this.postExtTypeNode(ext);
    }

    protected Ext postExtEnumDecl(Ext ext) {
        return this.postExtClassDecl(ext);
    }

    protected Ext postExtExtendedFor(Ext ext) {
        return this.postExtLoop(ext);
    }

    protected Ext postExtEnumConstantDecl(Ext ext) {
        return this.postExtClassMember(ext);
    }

    protected Ext postExtEnumConstant(Ext ext) {
        return this.postExtField(ext);
    }

    protected Ext postExtParamTypeNode(Ext ext) {
        return this.postExtTypeNode(ext);
    }

    protected Ext postExtAnnotationElemDecl(Ext ext) {
        return this.postExtClassMember(ext);
    }

    protected Ext postExtNormalAnnotationElem(Ext ext) {
        return this.postExtTerm(ext);
    }

    protected Ext postExtMarkerAnnotationElem(Ext ext) {
        return this.postExtNormalAnnotationElem(ext);
    }

    protected Ext postExtSingleElementAnnotationElem(Ext ext) {
        return this.postExtNormalAnnotationElem(ext);
    }

    protected Ext postExtElementValuePair(Ext ext) {
        return this.postExtTerm(ext);
    }

    protected Ext postExtElementValueArrayInit(Ext ext) {
        return this.postExtTerm(ext);
    }

}
