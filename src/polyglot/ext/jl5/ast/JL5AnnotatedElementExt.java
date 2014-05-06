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

import java.util.List;

import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.Declaration;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.ListUtil;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public abstract class JL5AnnotatedElementExt extends JL5TermExt implements
        AnnotatedElement {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<AnnotationElem> annotations;

    public JL5AnnotatedElementExt(List<AnnotationElem> annotations) {
        this.annotations = ListUtil.copy(annotations, true);
    }

    @Override
    public List<AnnotationElem> annotationElems() {
        return this.annotations;
    }

    @Override
    public Node annotationElems(List<AnnotationElem> annotations) {
        return annotationElems(node(), annotations);
    }

    protected <N extends Node> N annotationElems(N n,
            List<AnnotationElem> annotations) {
        JL5AnnotatedElementExt ext = (JL5AnnotatedElementExt) JL5Ext.ext(n);
        if (CollectionUtil.equals(ext.annotations, annotations)) return n;
        if (n == node) {
            n = Copy.Util.copy(n);
            ext = (JL5AnnotatedElementExt) JL5Ext.ext(n);
        }
        ext.annotations = ListUtil.copy(annotations, true);
        return n;
    }

    private Node reconstruct(Node n, List<AnnotationElem> annotations) {
        n = annotationElems(n, annotations);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Node n = superLang().visitChildren(this.node(), v);
        List<AnnotationElem> annots = visitList(annotations, v);
        return reconstruct(n, annots);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ts.checkDuplicateAnnotations(annotations);
        return superLang().typeCheck(this.node(), tc);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        for (Term ae : annotations) {
            tr.lang().prettyPrint(ae, w, tr);
            w.newline();
        }
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        Node n = this.node();
        for (AnnotationElem elem : annotations) {
            annoCheck.checkAnnotationApplicability(elem, this.declaration());
        }
        return n;
    }

    /**
     * Return the Declaration associated with this AST node.
     */
    protected abstract Declaration declaration();

    @Override
    public abstract void setAnnotations(Annotations annotations);

}
