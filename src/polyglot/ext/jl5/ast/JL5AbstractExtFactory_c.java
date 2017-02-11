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

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 =
                        ((JL5ExtFactory) nextExtFactory()).extAmbTypeInstantiation();
            }
            else {
                e2 = nextExtFactory().extTypeNode();
            }
            e = composeExts(e, e2);
        }
        return postExtAmbTypeInstantiation(e);
    }

    @Override
    public final Ext extAmbWildCard() {
        Ext e = extAmbWildCardImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 = ((JL5ExtFactory) nextExtFactory()).extAmbWildCard();
            }
            else {
                e2 = nextExtFactory().extTypeNode();
            }
            e = composeExts(e, e2);
        }
        return postExtAmbWildCard(e);
    }

    @Override
    public final Ext extEnumDecl() {
        Ext e = extEnumDeclImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 = ((JL5ExtFactory) nextExtFactory()).extEnumDecl();
            }
            else {
                e2 = nextExtFactory().extClassDecl();
            }
            e = composeExts(e, e2);
        }
        return postExtEnumDecl(e);
    }

    @Override
    public final Ext extExtendedFor() {
        Ext e = extExtendedForImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 = ((JL5ExtFactory) nextExtFactory()).extExtendedFor();
            }
            else {
                e2 = nextExtFactory().extLoop();
            }
            e = composeExts(e, e2);
        }
        return postExtExtendedFor(e);
    }

    @Override
    public final Ext extEnumConstantDecl() {
        Ext e = extEnumConstantDeclImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 = ((JL5ExtFactory) nextExtFactory()).extEnumConstantDecl();
            }
            else {
                e2 = nextExtFactory().extClassMember();
            }
            e = composeExts(e, e2);
        }
        return postExtEnumConstantDecl(e);
    }

    @Override
    public final Ext extEnumConstant() {
        Ext e = extEnumConstantImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 = ((JL5ExtFactory) nextExtFactory()).extEnumConstant();
            }
            else {
                e2 = nextExtFactory().extField();
            }
            e = composeExts(e, e2);
        }
        return postExtEnumConstant(e);
    }

    @Override
    public final Ext extParamTypeNode() {
        Ext e = extParamTypeNodeImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 = ((JL5ExtFactory) nextExtFactory()).extParamTypeNode();
            }
            else {
                e2 = nextExtFactory().extTypeNode();
            }
            e = composeExts(e, e2);
        }
        return postExtParamTypeNode(e);
    }

    @Override
    public final Ext extAnnotationElemDecl() {
        Ext e = extAnnotationElemDeclImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 = ((JL5ExtFactory) nextExtFactory()).extAnnotationElemDecl();
            }
            else {
                e2 = nextExtFactory().extMethodDecl();
            }
            e = composeExts(e, e2);
        }
        return postExtAnnotationElemDecl(e);
    }

    @Override
    public final Ext extNormalAnnotationElem() {
        Ext e = extNormalAnnotationElemImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 =
                        ((JL5ExtFactory) nextExtFactory()).extNormalAnnotationElem();
            }
            else {
                e2 = nextExtFactory().extTerm();
            }
            e = composeExts(e, e2);
        }
        return postExtNormalAnnotationElem(e);
    }

    @Override
    public final Ext extMarkerAnnotationElem() {
        Ext e = extMarkerAnnotationElemImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 =
                        ((JL5ExtFactory) nextExtFactory()).extMarkerAnnotationElem();
            }
            else {
                e2 = nextExtFactory().extTerm();
            }
            e = composeExts(e, e2);
        }
        return postExtMarkerAnnotationElem(e);
    }

    @Override
    public final Ext extSingleElementAnnotationElem() {
        Ext e = extSingleElementAnnotationElemImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 =
                        ((JL5ExtFactory) nextExtFactory()).extSingleElementAnnotationElem();
            }
            else {
                e2 = nextExtFactory().extTerm();
            }
            e = composeExts(e, e2);
        }
        return postExtSingleElementAnnotationElem(e);
    }

    @Override
    public final Ext extElementValuePair() {
        Ext e = extElementValuePairImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 = ((JL5ExtFactory) nextExtFactory()).extElementValuePair();
            }
            else {
                e2 = nextExtFactory().extTerm();
            }
            e = composeExts(e, e2);
        }
        return postExtElementValuePair(e);
    }

    @Override
    public final Ext extElementValueArrayInit() {
        Ext e = extElementValueArrayInitImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL5ExtFactory) {
                e2 =
                        ((JL5ExtFactory) nextExtFactory()).extElementValueArrayInit();
            }
            else {
                e2 = nextExtFactory().extTerm();
            }
            e = composeExts(e, e2);
        }
        return postExtElementValueArrayInit(e);
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
        return this.extClassMemberImpl();
    }

    protected Ext extEnumConstantImpl() {
        return this.extFieldImpl();
    }

    protected Ext extParamTypeNodeImpl() {
        return this.extTypeNodeImpl();
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

    protected Ext extAnnotationElemDeclImpl() {
        return this.extMethodDeclImpl();
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
        return this.postExtMethodDecl(ext);
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
