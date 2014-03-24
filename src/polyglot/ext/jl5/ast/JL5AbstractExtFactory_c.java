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
        return postExtAmbTypeInstantiation(e);
    }

    @Override
    public final Ext extAmbWildCard() {
        Ext e = extAmbWildCardImpl();
        return postExtAmbWildCard(e);
    }

    @Override
    public final Ext extEnumDecl() {
        Ext e = extEnumDeclImpl();
        return postExtEnumDecl(e);
    }

    @Override
    public final Ext extExtendedFor() {
        Ext e = extExtendedForImpl();
        return postExtExtendedFor(e);
    }

    @Override
    public final Ext extEnumConstantDecl() {
        Ext e = extEnumConstantDeclImpl();
        return postExtEnumConstantDecl(e);
    }

    @Override
    public final Ext extEnumConstant() {
        Ext e = extEnumConstantImpl();
        return postExtEnumConstant(e);
    }

    @Override
    public final Ext extParamTypeNode() {
        Ext e = extParamTypeNodeImpl();
        return postExtParamTypeNode(e);
    }

    @Override
    public final Ext extAnnotationElemDecl() {
        Ext e = extAnnotationElemDeclImpl();
        return postExtAnnotationElemDecl(e);
    }

    @Override
    public final Ext extNormalAnnotationElem() {
        Ext e = extNormalAnnotationElemImpl();
        return postExtNormalAnnotationElem(e);
    }

    @Override
    public final Ext extMarkerAnnotationElem() {
        Ext e = extMarkerAnnotationElemImpl();
        return postExtMarkerAnnotationElem(e);
    }

    @Override
    public final Ext extSingleElementAnnotationElem() {
        Ext e = extSingleElementAnnotationElemImpl();
        return postExtSingleElementAnnotationElem(e);
    }

    @Override
    public final Ext extElementValuePair() {
        Ext e = extElementValuePairImpl();
        return postExtElementValuePair(e);
    }

    @Override
    public final Ext extElementValueArrayInit() {
        Ext e = extElementValueArrayInitImpl();
        return postExtElementValueArrayInit(e);
    }

    protected static final Ext extAmbTypeInstantiation(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extAmbTypeInstantiation();
        return extTypeNode(extFactory);
    }

    protected static final Ext extAmbWildCard(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extAmbWildCard();
        return extTypeNode(extFactory);
    }

    protected static final Ext extEnumDecl(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extEnumDecl();
        return extClassDecl(extFactory);
    }

    protected static final Ext extExtendedFor(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extExtendedFor();
        return extLoop(extFactory);
    }

    protected static final Ext extEnumConstantDecl(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extEnumConstantDecl();
        return extClassMember(extFactory);
    }

    protected static final Ext extEnumConstant(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extEnumConstant();
        return extField(extFactory);
    }

    protected static final Ext extParamTypeNode(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extParamTypeNode();
        return extTypeNode(extFactory);
    }

    protected static final Ext extAnnotationElemDecl(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extAnnotationElemDecl();
        return extMethodDecl(extFactory);
    }

    protected static final Ext extNormalAnnotationElem(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extNormalAnnotationElem();
        return extTerm(extFactory);
    }

    protected static final Ext extMarkerAnnotationElem(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extMarkerAnnotationElem();
        return extTerm(extFactory);
    }

    protected static final Ext extSingleElementAnnotationElem(
            ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extSingleElementAnnotationElem();
        return extTerm(extFactory);
    }

    protected static final Ext extElementValuePair(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extElementValuePair();
        return extTerm(extFactory);
    }

    protected static final Ext extElementValueArrayInit(ExtFactory extFactory) {
        if (extFactory instanceof JL5ExtFactory)
            return ((JL5ExtFactory) extFactory).extElementValueArrayInit();
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

    public Ext postExtAmbTypeInstantiation(Ext ext) {
        return this.postExtTypeNode(ext);
    }

    public Ext postExtAmbWildCard(Ext ext) {
        return this.postExtTypeNode(ext);
    }

    public Ext postExtEnumDecl(Ext ext) {
        return this.postExtClassDecl(ext);
    }

    public Ext postExtExtendedFor(Ext ext) {
        return this.postExtLoop(ext);
    }

    public Ext postExtEnumConstantDecl(Ext ext) {
        return this.postExtClassMember(ext);
    }

    public Ext postExtEnumConstant(Ext ext) {
        return this.postExtField(ext);
    }

    public Ext postExtParamTypeNode(Ext ext) {
        return this.postExtTypeNode(ext);
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

    protected Ext postExtAnnotationElemDecl(Ext ext) {
        return ext;
    }

    protected Ext postExtNormalAnnotationElem(Ext ext) {
        return ext;
    }

    protected Ext postExtMarkerAnnotationElem(Ext ext) {
        return ext;
    }

    protected Ext postExtSingleElementAnnotationElem(Ext ext) {
        return ext;
    }

    protected Ext postExtElementValuePair(Ext ext) {
        return ext;
    }

    protected Ext postExtElementValueArrayInit(Ext ext) {
        return ext;
    }

}
