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
import polyglot.ast.FieldDecl;
import polyglot.ast.FieldDecl_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5FieldInstance;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class JL5FieldDecl_c extends FieldDecl_c implements FieldDecl,
        AnnotatedElement {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<AnnotationElem> annotations;

    public JL5FieldDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        super(pos, flags, type, name, init);
        if (annotations == null) {
            annotations = Collections.emptyList();
        }
        this.annotations = annotations;
    }

    @Override
    public List<AnnotationElem> annotationElems() {
        return this.annotations;
    }

    @Override
    public JL5FieldDecl_c annotationElems(List<AnnotationElem> annotations) {
        JL5FieldDecl_c n = (JL5FieldDecl_c) copy();
        n.annotations = ListUtil.copy(annotations, true);
        return n;
    }

    protected FieldDecl_c reconstruct(TypeNode type, Id name, Expr init,
            List<AnnotationElem> annotations) {
        if (this.type() != type || this.id() != name || this.init() != init
                || !CollectionUtil.equals(this.annotations, annotations)) {
            JL5FieldDecl_c n = (JL5FieldDecl_c) copy();
            n.type = type;
            n.name = name;
            n.init = init;
            n.annotations = ListUtil.copy(annotations, true);
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type(), v);
        Id name = (Id) visitChild(this.id(), v);
        Expr init = (Expr) visitChild(this.init(), v);
        List<AnnotationElem> annotations = visitList(this.annotations, v);
        return reconstruct(type, name, init, annotations);
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        for (AnnotationElem elem : annotations) {
            annoCheck.checkAnnotationApplicability(elem, this.fieldInstance());
        }
        return this;
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        JL5FieldInstance fi = (JL5FieldInstance) this.fieldInstance();
        fi.setAnnotations(annotations);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        for (AnnotationElem ae : annotations) {
            ae.prettyPrint(w, tr);
            w.newline();
        }

        super.prettyPrint(w, tr);
    }
}
