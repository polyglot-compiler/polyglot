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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.AnnotatedElement;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.types.AnnotationElementValue;
import polyglot.ext.jl5.types.AnnotationElementValueArray;
import polyglot.ext.jl5.types.AnnotationElementValueConstant;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Declaration;
import polyglot.types.FieldInstance;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.types.Package;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/** 
 *
 * Visitor that checks annotations of annotated elements 
 *
 */
public class AnnotationChecker extends ContextVisitor {
    public AnnotationChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        JL5Ext ext = JL5Ext.ext(n);
        if (ext instanceof AnnotatedElement) {
            AnnotatedElement ae = (AnnotatedElement) ext;
            return ae.annotationCheck((AnnotationChecker) v);
        }
        return n;
    }

    @Override
    public JL5TypeSystem typeSystem() {
        return (JL5TypeSystem) super.typeSystem();
    }

    /** 
     * 
     * Is the annotation element {@code annotation} applicable
     * to Declaration decl? For example, if annotation is "@Override" then decl
     * better be a methodInstance that overrides another method. If annotation's
     * type itself has annotations describing which targets are appropriate,
     * then decl must be an appropriate target.
     * 
     * @param annotation
     * @param decl
     * @throws SemanticException
     */
    public void checkAnnotationApplicability(AnnotationElem n, Declaration decl)
            throws SemanticException {
        AnnotationElem annotation = n;
        JL5ClassType annotationType =
                (JL5ClassType) annotation.typeName().type().toClass();

        if (annotationType.equals(typeSystem().OverrideAnnotation())) {
            checkOverrideAnnotation(decl);
        }

        // If annotationType specifies what kind of target it is meant to be applied to,
        // then check that.

        Annotations ra = annotationType.annotations();
        if (ra != null) {
            for (Type at : ra.annotationTypes()) {
                if (at.equals(typeSystem().TargetAnnotation())) {
                    // annotationType has a target annotation!
                    checkTargetMetaAnnotation((AnnotationElementValueArray) ra.singleElement(at),
                                              n,
                                              decl);
                }
            }
        }
    }

    protected void checkOverrideAnnotation(Declaration decl)
            throws SemanticException {
        if (!(decl instanceof MethodInstance)) {
            throw new SemanticException("An override annotation can apply only to methods.",
                                        decl.position());
        }
        MethodInstance mi = (MethodInstance) decl;
        JL5TypeSystem ts = this.typeSystem();
        List<MethodInstance> overrides = new LinkedList<>(ts.implemented(mi));
        overrides.remove(mi);
        if (overrides.isEmpty()) {
            throw new SemanticException("Method " + mi.signature()
                    + " does not override a method.", decl.position());
        }
    }

    protected void checkTargetMetaAnnotation(
            AnnotationElementValueArray targetKinds, AnnotationElem n,
            Declaration decl) throws SemanticException {
        AnnotationElem annotation = n;
        Collection<EnumInstance> eis =
                annotationElementTypesForDeclaration(decl);
        // the array targs must contain at least one of the eis.
        boolean foundAppropriateTarget = false;
        requiredCheck: for (EnumInstance required : eis) {
            for (AnnotationElementValue found : targetKinds.vals()) {
                AnnotationElementValueConstant c =
                        (AnnotationElementValueConstant) found;
                if (required.equals(c.constantValue())) {
                    foundAppropriateTarget = true;
                    break requiredCheck;
                }
            }
        }

        if (!foundAppropriateTarget) {
            throw new SemanticException("Annotation "
                                                + annotation
                                                + " not applicable to this kind of declaration.",
                                        n.position());
        }

    }

    public Collection<EnumInstance> annotationElementTypesForDeclaration(
            Declaration decl) {
        ClassType aet = typeSystem().AnnotationElementType();
        if (decl instanceof MethodInstance) {
            return Collections.singleton((EnumInstance) aet.fieldNamed("METHOD"));
        }
        if (decl instanceof FieldInstance) {
            return Collections.singleton((EnumInstance) aet.fieldNamed("FIELD"));
        }
        if (decl instanceof LocalInstance) {
            // it's either a local instance or a formal
            // Check the local instance info
            JL5LocalInstance li = (JL5LocalInstance) decl;
            if (li.isProcedureFormal()) {
                return Collections.singleton((EnumInstance) aet.fieldNamed("PARAMETER"));
            }
            else {
                return Collections.singleton((EnumInstance) aet.fieldNamed("LOCAL_VARIABLE"));
            }
        }
        if (decl instanceof ClassType) {
            ClassType ct = (ClassType) decl;
            if (ct.flags().isInterface() && JL5Flags.isAnnotation(ct.flags())) {
                // it's an annotation
                return Arrays.asList(new EnumInstance[] {
                        (EnumInstance) aet.fieldNamed("TYPE"),
                        (EnumInstance) aet.fieldNamed("ANNOTATION_TYPE") });
            }
            return Collections.singleton((EnumInstance) aet.fieldNamed("TYPE"));
        }
        if (decl instanceof ConstructorInstance) {
            return Collections.singleton((EnumInstance) aet.fieldNamed("CONSTRUCTOR"));
        }
        if (decl instanceof Package) {
            return Collections.singleton((EnumInstance) aet.fieldNamed("PACKAGE"));
        }

        throw new InternalCompilerError("Don't know how to deal with " + decl);
    }
}
