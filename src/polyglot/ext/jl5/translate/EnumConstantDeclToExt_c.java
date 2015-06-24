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

import java.util.List;

import polyglot.ast.Node;
import polyglot.ext.jl5.ast.AnnotatedElement;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;

public class EnumConstantDeclToExt_c extends ToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        EnumConstantDecl cd = (EnumConstantDecl) node();
        List<AnnotationElem> annotationElems =
                ((AnnotatedElement) JL5Ext.ext(cd)).annotationElems();
        rw = (ExtensionRewriter) rw.bypass(annotationElems);
        return ((JL5NodeFactory) rw.to_nf()).EnumConstantDecl(cd.position(),
                                                              cd.flags(),
                                                              annotationElems,
                                                              cd.name(),
                                                              cd.args(),
                                                              cd.body(),
                                                              cd.javadoc());
    }
}
