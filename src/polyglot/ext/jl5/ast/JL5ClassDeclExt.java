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

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.ClassType;
import polyglot.types.Declaration;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;

public class JL5ClassDeclExt extends JL5AnnotatedElementExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<ParamTypeNode> paramTypes = new ArrayList<ParamTypeNode>();

    public List<ParamTypeNode> paramTypes() {
        return this.paramTypes;
    }

    public ClassDecl paramTypes(List<ParamTypeNode> types) {
        ClassDecl n = (ClassDecl) this.node().copy();
        JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(n);
        ext.paramTypes = types;
        return n;
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        super.annotationCheck(annoCheck);

        ClassDecl n = (ClassDecl) this.node();

        // check annotation circularity
        if (JL5Flags.isAnnotation(n.flags())) {
            JL5ParsedClassType ct = (JL5ParsedClassType) n.type();
            for (AnnotationTypeElemInstance ai : ct.annotationElems()) {
                if (ai.type() instanceof ClassType
                        && ((ClassType) ((ClassType) ai.type()).superType()).fullName()
                                                                            .equals("java.lang.annotation.Annotation")) {
                    JL5ParsedClassType other = (JL5ParsedClassType) ai.type();
                    for (Object element2 : other.annotationElems()) {
                        AnnotationTypeElemInstance aj =
                                (AnnotationTypeElemInstance) element2;
                        if (aj.type().equals(ct)) {
                            throw new SemanticException("cyclic annotation element type",
                                                        aj.position());
                        }
                    }
                }
            }
        }
        return n;
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        ClassDecl n = (ClassDecl) this.node();
        JL5ParsedClassType pct = (JL5ParsedClassType) n.type();
        pct.setAnnotations(annotations);
        pct.setAnnotationsResolved(true);
    }

    @Override
    protected Declaration declaration() {
        ClassDecl n = (ClassDecl) this.node();
        return n.type();
    }
}
