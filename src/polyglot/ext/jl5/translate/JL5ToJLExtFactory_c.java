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
package polyglot.ext.jl5.translate;

import polyglot.ast.Ext;
import polyglot.ext.jl5.ast.JL5ExtFactory;
import polyglot.translate.ext.CallToExt_c;
import polyglot.translate.ext.ConstructorCallToExt_c;

public class JL5ToJLExtFactory_c extends JL5ToExtFactory_c {

    public JL5ToJLExtFactory_c() {
        super();
    }

    public JL5ToJLExtFactory_c(JL5ExtFactory extFactory) {
        super(extFactory);
    }

    @Override
    protected Ext extCallImpl() {
        return new CallToExt_c();
    }

    @Override
    protected Ext extConstructorCallImpl() {
        return new ConstructorCallToExt_c();
    }

    @Override
    protected Ext extClassDeclImpl() {
        return new JL5ClassDeclToJL_c();
    }

    @Override
    protected Ext extMethodDeclImpl() {
        return new JL5MethodDeclToJL_c();
    }

    @Override
    protected Ext extConstructorDeclImpl() {
        return new JL5ConstructorDeclToJL_c();
    }

    @Override
    protected Ext extCanonicalTypeNodeImpl() {
        return new JL5TypeNodeToJL_c();
    }

    @Override
    protected Ext extParamTypeNodeImpl() {
        return new JL5TypeNodeToJL_c();
    }

    @Override
    protected Ext extEnumConstantImpl() {
        return new EnumConstantToJL_c();
    }

    @Override
    protected Ext extFieldDeclImpl() {
        return new JL5FieldDeclToJL_c();
    }

    @Override
    protected Ext extFormalImpl() {
        return new JL5FormalToJL_c();
    }

    @Override
    protected Ext extLocalDeclImpl() {
        return new JL5LocalDeclToJL_c();
    }

    // The below nodes should have been removed 
    // by the time the ExtensionRewriter is called.
    @Override
    protected Ext extEnumDeclImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extExtendedForImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extEnumConstantDeclImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extAnnotationElemDeclImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extNormalAnnotationElemImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extElementValuePairImpl() {
        return new CannotToExt_c();
    }

}
