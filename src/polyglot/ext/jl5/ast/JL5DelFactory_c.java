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
import polyglot.ast.JL;

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
    public JL delEnumDecl() {
        JL e = delEnumDeclImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delEnumDecl();
            e = composeDels(e, e2);
        }
        return postDelEnumDecl(e);
    }

    @Override
    public JL delExtendedFor() {
        JL e = delExtendedForImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delExtendedFor();
            e = composeDels(e, e2);
        }
        return postDelExtendedFor(e);
    }

    @Override
    public JL delEnumConstantDecl() {
        JL e = delEnumConstantImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delEnumConstant();
            e = composeDels(e, e2);
        }
        return postDelEnumConstant(e);
    }

    @Override
    public JL delEnumConstant() {
        JL e = delEnumConstantImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delEnumConstant();
            e = composeDels(e, e2);
        }
        return postDelEnumConstant(e);
    }

    @Override
    public JL delParamTypeNode() {
        JL e = delParamTypeNodeImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delParamTypeNode();
            e = composeDels(e, e2);
        }
        return postDelParamTypeNode(e);
    }

    @Override
    public JL delAnnotationElemDecl() {
        JL e = delAnnotationElemDeclImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delAnnotationElemDecl();
            e = composeDels(e, e2);
        }
        return postDelAnnotationElemDecl(e);
    }

    @Override
    public JL delNormalAnnotationElem() {
        JL e = delNormalAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delNormalAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelNormalAnnotationElem(e);
    }

    @Override
    public JL delMarkerAnnotationElem() {
        JL e = delMarkerAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delMarkerAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelMarkerAnnotationElem(e);
    }

    @Override
    public JL delSingleElementAnnotationElem() {
        JL e = delSingleElementAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delSingleElementAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelSingleElementAnnotationElem(e);
    }

    @Override
    public JL delElementValuePair() {
        JL e = delElementValuePairImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delElementValuePair();
            e = composeDels(e, e2);
        }
        return postDelElementValuePair(e);
    }

    @Override
    public JL delElementValueArrayInit() {
        JL e = delElementValueArrayInitImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delElementValueArrayInit();
            e = composeDels(e, e2);
        }
        return postDelElementValueArrayInit(e);
    }

    public JL delEnumDeclImpl() {
        return this.delClassDeclImpl();
    }

    public JL delExtendedForImpl() {
        return this.delLoopImpl();
    }

    public JL delEnumConstantDeclImpl() {
        return this.delClassMemberImpl();
    }

    public JL delEnumConstantImpl() {
        return this.delFieldImpl();
    }

    public JL delParamTypeNodeImpl() {
        return this.delTypeNodeImpl();
    }

    @Override
    protected JL delAssignImpl() {
        return new JL5AssignDel();
    }

    @Override
    protected JL delBinaryImpl() {
        return new JL5BinaryDel();
    }

    @Override
    protected JL delNodeImpl() {
        return new JL5Del();
    }

    public JL postDelEnumDecl(JL del) {
        return this.postDelClassDecl(del);
    }

    public JL postDelExtendedFor(JL del) {
        return this.postDelLoop(del);
    }

    public JL postDelEnumConstantDecl(JL del) {
        return this.postDelClassMember(del);
    }

    public JL postDelEnumConstant(JL del) {
        return this.postDelField(del);
    }

    public JL postDelParamTypeNode(JL del) {
        return this.postDelTypeNode(del);
    }

    private JL postDelAnnotationElemDecl(JL e) {
        return e;
    }

    private JL delAnnotationElemDeclImpl() {
        return this.delClassMemberImpl();
    }

    private JL delNormalAnnotationElemImpl() {
        return this.delTermImpl();
    }

    private JL delMarkerAnnotationElemImpl() {
        return this.delNormalAnnotationElemImpl();
    }

    private JL delSingleElementAnnotationElemImpl() {
        return this.delNormalAnnotationElemImpl();
    }

    private JL delElementValuePairImpl() {
        return this.delTermImpl();
    }

    private JL delElementValueArrayInitImpl() {
        return this.delTermImpl();
    }

    private JL postDelNormalAnnotationElem(JL e) {
        return e;
    }

    private JL postDelMarkerAnnotationElem(JL e) {
        return e;
    }

    private JL postDelSingleElementAnnotationElem(JL e) {
        return e;
    }

    private JL postDelElementValuePair(JL e) {
        return e;
    }

    private JL postDelElementValueArrayInit(JL e) {
        return e;
    }

}
