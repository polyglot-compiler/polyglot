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
package polyglot.ext.jl5.visit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Node;
import polyglot.ext.jl5.ast.AnnotatedElement;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.types.AnnotationElementValue;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;

/**
 * Simplify some expressions for the later analyses. Actually, this is a kitchen-sink
 * clean up pass...
 * @param <E>
 */
public class ResolveAnnotationsVisitor extends ContextVisitor {
    public ResolveAnnotationsVisitor(Job job) {
        super(job, job.extensionInfo().typeSystem(), job.extensionInfo()
                                                        .nodeFactory());
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        JL5Ext ext = JL5Ext.ext(n);
        if (ext instanceof AnnotatedElement) {
            AnnotatedElement aext = (AnnotatedElement) ext;
            List<AnnotationElem> newElems =
                    new ArrayList<>(aext.annotationElems().size());
            for (AnnotationElem elem : aext.annotationElems()) {
                // type check elem
                TypeChecker tc =
                        new TypeChecker(this.job(),
                                        this.typeSystem(),
                                        this.nodeFactory());

                tc = (TypeChecker) tc.context(this.context());
                elem = (AnnotationElem) elem.visit(tc);
                AnnotationElem ae = elem;
                if (!ae.typeName().type().isCanonical()) {
                    throw new InternalCompilerError("Couldn't type check "
                                                            + elem
                                                            + " during annotation resolution",
                                                    elem.position());

                }
                newElems.add(elem);
            }
            n = aext.annotationElems(newElems);
            aext.setAnnotations(createAnnotations(newElems, n.position()));
            return n;
        }
        return n;
    }

    /**
     * Given a list of annotation elements, create an Annotations
     * for the annotations that should survive in the binary (i.e., in the
     * type information)
     * @throws SemanticException 
     */
    public Annotations createAnnotations(List<AnnotationElem> annotationElems,
            Position pos) throws SemanticException {
        Map<Type, Map<String, AnnotationElementValue>> m =
                new LinkedHashMap<>();

        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
        for (AnnotationElem ae : annotationElems) {
            Type annotationType = ae.typeName().type();
            m.put(annotationType, ae.toAnnotationElementValues(lang(), ts));
        }
        return ts.createAnnotations(m, pos);
    }

}
