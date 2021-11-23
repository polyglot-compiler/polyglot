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
package polyglot.ext.jl8.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ext.jl7.ast.JL7AbstractExtFactory_c;

public abstract class JL8AbstractExtFactory_c extends JL7AbstractExtFactory_c
        implements JL8ExtFactory {

    public JL8AbstractExtFactory_c() {
        super();
    }

    public JL8AbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    public final Ext extFunctionValueNode() {
        Ext e = extFunctionValueNodeImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL8ExtFactory) {
                e2 = ((JL8ExtFactory) nextExtFactory()).extFunctionValueNode();
            } else {
                e2 = nextExtFactory().extExpr();
            }
            e = composeExts(e, e2);
        }
        return e;
    }

    protected Ext extFunctionValueNodeImpl() {
        return extExpr();
    }

    @Override
    public final Ext extLambdaExpressionNode() {
        Ext e = extLambdaExpressionNodeImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL8ExtFactory) {
                e2 = ((JL8ExtFactory) nextExtFactory()).extLambdaExpressionNode();
            } else {
                e2 = nextExtFactory().extTerm();
            }
            e = composeExts(e, e2);
        }
        return e;
    }

    protected Ext extLambdaExpressionNodeImpl() {
        return extTerm();
    }

    @Override
    public final Ext extMethodReferenceNode() {
        Ext e = extMethodReferenceNodeImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL8ExtFactory) {
                e2 = ((JL8ExtFactory) nextExtFactory()).extMethodReferenceNode();
            } else {
                e2 = nextExtFactory().extTerm();
            }
            e = composeExts(e, e2);
        }
        return e;
    }

    protected Ext extMethodReferenceNodeImpl() {
        return extTerm();
    }
}
