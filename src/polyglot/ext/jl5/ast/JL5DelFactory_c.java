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

import polyglot.ast.AbstractDelFactory_c;
import polyglot.ast.JLDel;

public class JL5DelFactory_c extends AbstractDelFactory_c implements
        JL5DelFactory {

    public JL5DelFactory_c() {
        super();
    }

    public JL5DelFactory_c(JL5DelFactory delFactory) {
        super(delFactory);
    }

    @Override
    public JL5DelFactory nextDelFactory() {
        return (JL5DelFactory) super.nextDelFactory();
    }

    @Override
    public JLDel delEnumDecl() {
        JLDel e = delEnumDeclImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delEnumDecl();
            e = composeDels(e, e2);
        }
        return postDelEnumDecl(e);
    }

    @Override
    public JLDel delExtendedFor() {
        JLDel e = delExtendedForImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delExtendedFor();
            e = composeDels(e, e2);
        }
        return postDelExtendedFor(e);
    }

    @Override
    public JLDel delEnumConstantDecl() {
        JLDel e = delEnumConstantDeclImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delEnumConstant();
            e = composeDels(e, e2);
        }
        return postDelEnumConstant(e);
    }

    @Override
    public JLDel delEnumConstant() {
        JLDel e = delEnumConstantImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delEnumConstant();
            e = composeDels(e, e2);
        }
        return postDelEnumConstant(e);
    }

    @Override
    public JLDel delParamTypeNode() {
        JLDel e = delParamTypeNodeImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delParamTypeNode();
            e = composeDels(e, e2);
        }
        return postDelParamTypeNode(e);
    }

    @Override
    public JLDel delAnnotationElemDecl() {
        JLDel e = delAnnotationElemDeclImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delAnnotationElemDecl();
            e = composeDels(e, e2);
        }
        return postDelAnnotationElemDecl(e);
    }

    @Override
    public JLDel delNormalAnnotationElem() {
        JLDel e = delNormalAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delNormalAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelNormalAnnotationElem(e);
    }

    @Override
    public JLDel delMarkerAnnotationElem() {
        JLDel e = delMarkerAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delMarkerAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelMarkerAnnotationElem(e);
    }

    @Override
    public JLDel delSingleElementAnnotationElem() {
        JLDel e = delSingleElementAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delSingleElementAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelSingleElementAnnotationElem(e);
    }

    @Override
    public JLDel delElementValuePair() {
        JLDel e = delElementValuePairImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delElementValuePair();
            e = composeDels(e, e2);
        }
        return postDelElementValuePair(e);
    }

    @Override
    public JLDel delElementValueArrayInit() {
        JLDel e = delElementValueArrayInitImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delElementValueArrayInit();
            e = composeDels(e, e2);
        }
        return postDelElementValueArrayInit(e);
    }

    public JLDel delEnumDeclImpl() {
        return new JL5EnumDeclDel();
    }

    public JLDel delExtendedForImpl() {
        return this.delLoopImpl();
    }

    public JLDel delEnumConstantDeclImpl() {
        return this.delClassMemberImpl();
    }

    public JLDel delEnumConstantImpl() {
        return this.delFieldImpl();
    }

    public JLDel delParamTypeNodeImpl() {
        return this.delTypeNodeImpl();
    }

    @Override
    protected JLDel delAssertImpl() {
        return new JL5AssertDel();
    }

    @Override
    protected JLDel delAssignImpl() {
        return new JL5AssignDel();
    }

    @Override
    protected JLDel delBinaryImpl() {
        return new JL5BinaryDel();
    }

    @Override
    protected JLDel delClassBodyImpl() {
        return new JL5ClassBodyDel();
    }

    @Override
    protected JLDel delCaseImpl() {
        return new JL5CaseDel();
    }

    @Override
    protected JLDel delCatchImpl() {
        return new JL5CatchDel();
    }

    @Override
    protected JLDel delConstructorCallImpl() {
        return new JL5ConstructorCallDel();
    }

    @Override
    protected JLDel delCanonicalTypeNodeImpl() {
        return new JL5CanonicalTypeNodeDel();
    }

    @Override
    protected JLDel delCallImpl() {
        return new JL5CallDel();
    }

    @Override
    protected JLDel delSwitchImpl() {
        return new JL5SwitchDel();
    }

    @Override
    protected JLDel delTryImpl() {
        return new JL5TryDel();
    }

    @Override
    protected JLDel delClassLitImpl() {
        return new JL5ClassLitDel();
    }

    @Override
    protected JLDel delConditionalImpl() {
        return new JL5ConditionalDel();
    }

    @Override
    protected JLDel delFieldImpl() {
        return new JL5FieldDel();
    }

    @Override
    protected JLDel delFormalImpl() {
        return new JL5FormalDel();
    }

    @Override
    protected JLDel delImportImpl() {
        return new JL5ImportDel();
    }

    @Override
    protected JLDel delFieldDeclImpl() {
        return new JL5FieldDeclDel();
    }

    @Override
    protected JLDel delLocalDeclImpl() {
        return new JL5LocalDeclDel();
    }

    @Override
    protected JLDel delClassDeclImpl() {
        return new JL5ClassDeclDel();
    }

    @Override
    protected JLDel delMethodDeclImpl() {
        return new JL5MethodDeclDel();
    }

    @Override
    protected JLDel delConstructorDeclImpl() {
        return new JL5ConstructorDeclDel();
    }

    @Override
    protected JLDel delNodeImpl() {
        return new JL5Del();
    }

    @Override
    protected JLDel delNewImpl() {
        return new JL5NewDel();
    }

    @Override
    protected JLDel delNewArrayImpl() {
        return new JL5NewArrayDel();
    }

    @Override
    protected JLDel delSpecialImpl() {
        return new JL5SpecialDel();
    }

    @Override
    protected JLDel delUnaryImpl() {
        return new JL5UnaryDel();
    }

    public JLDel postDelEnumDecl(JLDel del) {
        return this.postDelClassDecl(del);
    }

    public JLDel postDelExtendedFor(JLDel del) {
        return this.postDelLoop(del);
    }

    public JLDel postDelEnumConstantDecl(JLDel del) {
        return this.postDelClassMember(del);
    }

    public JLDel postDelEnumConstant(JLDel del) {
        return this.postDelField(del);
    }

    public JLDel postDelParamTypeNode(JLDel del) {
        return this.postDelTypeNode(del);
    }

    private JLDel postDelAnnotationElemDecl(JLDel e) {
        return e;
    }

    private JLDel delAnnotationElemDeclImpl() {
        return this.delClassMemberImpl();
    }

    private JLDel delNormalAnnotationElemImpl() {
        return this.delTermImpl();
    }

    private JLDel delMarkerAnnotationElemImpl() {
        return this.delNormalAnnotationElemImpl();
    }

    private JLDel delSingleElementAnnotationElemImpl() {
        return this.delNormalAnnotationElemImpl();
    }

    private JLDel delElementValuePairImpl() {
        return this.delTermImpl();
    }

    private JLDel delElementValueArrayInitImpl() {
        return this.delTermImpl();
    }

    private JLDel postDelNormalAnnotationElem(JLDel e) {
        return e;
    }

    private JLDel postDelMarkerAnnotationElem(JLDel e) {
        return e;
    }

    private JLDel postDelSingleElementAnnotationElem(JLDel e) {
        return e;
    }

    private JLDel postDelElementValuePair(JLDel e) {
        return e;
    }

    private JLDel postDelElementValueArrayInit(JLDel e) {
        return e;
    }

}
