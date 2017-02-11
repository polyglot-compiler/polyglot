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
package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ext.jl5.ast.JL5AbstractExtFactory_c;

public abstract class JL7AbstractExtFactory_c extends JL5AbstractExtFactory_c
        implements JL7ExtFactory {

    public JL7AbstractExtFactory_c() {
        super();
    }

    public JL7AbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    public final Ext extAmbDiamondTypeNode() {
        Ext e = extAmbDiamondTypeNodeImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL7ExtFactory) {
                e2 = ((JL7ExtFactory) nextExtFactory()).extAmbDiamondTypeNode();
            }
            else {
                e2 = nextExtFactory().extTypeNode();
            }
            e = composeExts(e, e2);
        }
        return postExtAmbDiamondTypeNode(e);
    }

    @Override
    public final Ext extAmbUnionType() {
        Ext e = extAmbUnionTypeImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL7ExtFactory) {
                e2 = ((JL7ExtFactory) nextExtFactory()).extAmbUnionType();
            }
            else {
                e2 = nextExtFactory().extTypeNode();
            }
            e = composeExts(e, e2);
        }
        return postExtAmbUnionType(e);
    }

    @Override
    public final Ext extMultiCatch() {
        Ext e = extMultiCatchImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL7ExtFactory) {
                e2 = ((JL7ExtFactory) nextExtFactory()).extMultiCatch();
            }
            else {
                e2 = nextExtFactory().extCatch();
            }
            e = composeExts(e, e2);
        }
        return postExtMultiCatch(e);
    }

    @Override
    public final Ext extResource() {
        Ext e = extResourceImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL7ExtFactory) {
                e2 = ((JL7ExtFactory) nextExtFactory()).extResource();
            }
            else {
                e2 = nextExtFactory().extLocalDecl();
            }
            e = composeExts(e, e2);
        }
        return postExtResource(e);
    }

    @Override
    public final Ext extTryWithResources() {
        Ext e = extTryWithResourcesImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL7ExtFactory) {
                e2 = ((JL7ExtFactory) nextExtFactory()).extTryWithResources();
            }
            else {
                e2 = nextExtFactory().extTry();
            }
            e = composeExts(e, e2);
        }
        return postExtTryWithResources(e);
    }

    protected Ext extAmbDiamondTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    protected Ext extAmbUnionTypeImpl() {
        return extTypeNodeImpl();
    }

    protected Ext extMultiCatchImpl() {
        return extCatchImpl();
    }

    protected Ext extResourceImpl() {
        return extLocalDeclImpl();
    }

    protected Ext extTryWithResourcesImpl() {
        return extTryImpl();
    }

    protected Ext postExtAmbDiamondTypeNode(Ext e) {
        return postExtTypeNode(e);
    }

    protected Ext postExtAmbUnionType(Ext e) {
        return postExtTypeNode(e);
    }

    protected Ext postExtMultiCatch(Ext e) {
        return postExtCatch(e);
    }

    protected Ext postExtResource(Ext e) {
        return postExtLocalDecl(e);
    }

    protected Ext postExtTryWithResources(Ext e) {
        return postExtTry(e);
    }
}
