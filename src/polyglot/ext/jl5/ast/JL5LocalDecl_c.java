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

import java.util.Collections;
import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.LocalDecl;
import polyglot.ast.LocalDecl_c;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.TypeVariable.TVarDecl;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;

public class JL5LocalDecl_c extends LocalDecl_c implements LocalDecl,
        AnnotatedElement {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<AnnotationElem> annotations;

    public JL5LocalDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        super(pos, flags, type, name, init);
        if (annotations == null) {
            annotations = Collections.emptyList();
        }
        this.annotations = annotations;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == init) {
            TypeSystem ts = av.typeSystem();

            // If the RHS is an integral constant, we can relax the expected
            // type to the type of the constant, provided that no autoboxing
            // is involved.
            if (ts.numericConversionValid(type.type(), child.constantValue())) {
                if (child.type().isPrimitive() && type.type().isPrimitive()) {
                    return child.type();
                }
            }
            return type.type();
        }

        return child.type();
    }

    @Override
    public List<AnnotationElem> annotationElems() {
        return annotations;
    }

    @Override
    public JL5LocalDecl_c annotationElems(List<AnnotationElem> annotations) {
        JL5LocalDecl_c n = (JL5LocalDecl_c) copy();
        n.annotations = annotations;
        return n;
    }

    protected LocalDecl reconstruct(TypeNode type, Expr init,
            List<AnnotationElem> annotations, Id name) {
        if (this.type() != type || this.init() != init
                || !CollectionUtil.equals(annotations, this.annotations)
                || this.id() != name) {
            JL5LocalDecl_c n = (JL5LocalDecl_c) copy();
            n.type = type;
            n.init = init;
            n.annotations = annotations;
            n.name = name;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type(), v);
        Id name = (Id) visitChild(this.id(), v);
        Expr init = (Expr) visitChild(this.init(), v);
        List<AnnotationElem> annots = visitList(this.annotations, v);
        return reconstruct(type, init, annots, name);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (!flags().clear(Flags.FINAL).equals(Flags.NONE)) {
            throw new SemanticException("Modifier: " + flags().clearFinal()
                    + " not allowed here.", position());
        }
        if (type().type() instanceof TypeVariable
                && tc.context().inStaticContext()) {
            if (((TypeVariable) type().type()).declaredIn()
                                              .equals(TVarDecl.CLASS_TYPE_VARIABLE))
                throw new SemanticException("Cannot access non-static type: "
                        + ((TypeVariable) type().type()).name()
                        + " in a static context.", position());
        }
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ts.checkDuplicateAnnotations(annotations);
        return super.typeCheck(tc);
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        JL5LocalInstance li = (JL5LocalInstance) this.localInstance();
        li.setAnnotations(annotations);
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        for (AnnotationElem element : annotations) {
            annoCheck.checkAnnotationApplicability(element,
                                                   this.localInstance());
        }
        return this;
    }
}
