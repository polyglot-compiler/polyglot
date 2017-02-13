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

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public final class JL5ExtFactory_c extends JL5AbstractExtFactory_c {

    public JL5ExtFactory_c() {
        super();
    }

    public JL5ExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        return new JL5Ext();
    }

    @Override
    protected Ext extAnnotationElemDeclImpl() {
        // AnnotationElemDecl doesn't use JL5MethodDeclExt; bypass.
        return extClassMemberImpl();
    }

    @Override
    protected Ext extAssertImpl() {
        return new JL5AssertExt();
    }

    @Override
    protected Ext extAssignImpl() {
        return new JL5AssignExt();
    }

    @Override
    protected Ext extBinaryImpl() {
        return new JL5BinaryExt();
    }

    @Override
    protected Ext extCallImpl() {
        return new JL5CallExt();
    }

    @Override
    protected Ext extCanonicalTypeNodeImpl() {
        return new JL5CanonicalTypeNodeExt();
    }

    @Override
    protected Ext extCaseImpl() {
        return new JL5CaseExt();
    }

    @Override
    protected Ext extCatchImpl() {
        return new JL5CatchExt();
    }

    @Override
    protected Ext extClassBodyImpl() {
        return new JL5ClassBodyExt();
    }

    @Override
    protected Ext extClassDeclImpl() {
        return new JL5ClassDeclExt();
    }

    @Override
    protected Ext extClassLitImpl() {
        return new JL5ClassLitExt();
    }

    @Override
    protected Ext extConditionalImpl() {
        return new JL5ConditionalExt();
    }

    @Override
    protected Ext extConstructorCallImpl() {
        return new JL5ConstructorCallExt();
    }

    @Override
    protected Ext extConstructorDeclImpl() {
        return new JL5ConstructorDeclExt();
    }

    @Override
    protected Ext extEnumConstantDeclImpl() {
        return new EnumConstantDeclExt();
    }

    @Override
    protected Ext extEnumDeclImpl() {
        return new JL5EnumDeclExt();
    }

    @Override
    protected Ext extExprImpl() {
        return new JL5ExprExt();
    }

    @Override
    protected Ext extFieldImpl() {
        return new JL5FieldExt();
    }

    @Override
    protected Ext extFieldDeclImpl() {
        return new JL5FieldDeclExt();
    }

    @Override
    protected Ext extFormalImpl() {
        return new JL5FormalExt();
    }

    @Override
    protected Ext extImportImpl() {
        return new JL5ImportExt();
    }

    @Override
    protected Ext extInstanceofImpl() {
        return new JL5InstanceofExt();
    }

    @Override
    protected Ext extLocalDeclImpl() {
        return new JL5LocalDeclExt();
    }

    @Override
    protected Ext extLoopImpl() {
        return new JL5LoopExt();
    }

    @Override
    protected Ext extMethodDeclImpl() {
        return new JL5MethodDeclExt();
    }

    @Override
    protected Ext extNewImpl() {
        return new JL5NewExt();
    }

    @Override
    protected Ext extNewArrayImpl() {
        return new JL5NewArrayExt();
    }

    @Override
    protected Ext extSourceFileImpl() {
        return new JL5SourceFileExt();
    }

    @Override
    protected Ext extSpecialImpl() {
        return new JL5SpecialExt();
    }

    @Override
    protected Ext extSwitchImpl() {
        return new JL5SwitchExt();
    }

    @Override
    protected Ext extTermImpl() {
        return new JL5TermExt();
    }

    @Override
    protected Ext extUnaryImpl() {
        return new JL5UnaryExt();
    }
}
