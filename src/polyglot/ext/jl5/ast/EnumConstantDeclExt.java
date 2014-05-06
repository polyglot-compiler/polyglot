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
import polyglot.ext.jl5.types.Annotations;
import polyglot.types.Declaration;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.BodyDisambiguator;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.PruningVisitor;
import polyglot.visit.SignatureDisambiguator;
import polyglot.visit.SupertypeDisambiguator;

/**
 * This class mainly exists so that EnumConstantDecls can be identified as
 * being Annotated Elements by examining their JL5Ext.
 *
 */
public class EnumConstantDeclExt extends JL5AnnotatedElementExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public EnumConstantDeclExt() {
        this(null);
    }

    public EnumConstantDeclExt(List<AnnotationElem> annotations) {
        super(annotations);
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        EnumConstantDecl nn = (EnumConstantDecl) this.node();

        BodyDisambiguator bd = new BodyDisambiguator(ar);
        NodeVisitor childv = bd.enter(parent, nn);

        if (childv instanceof PruningVisitor) {
            return nn;
        }

        BodyDisambiguator childbd = (BodyDisambiguator) childv;

        // Now disambiguate the actuals.
        nn = nn.args(nn.visitList(nn.args(), childbd));

        if (nn.body() != null) {
            SupertypeDisambiguator supDisamb =
                    new SupertypeDisambiguator(childbd);
            nn = nn.body(nn.visitChild(nn.body(), supDisamb));

            SignatureDisambiguator sigDisamb =
                    new SignatureDisambiguator(childbd);
            nn = nn.body(nn.visitChild(nn.body(), sigDisamb));

            // Now visit the body.
            nn = nn.body(nn.visitChild(nn.body(), childbd));
        }

        // Now visit the annotations
        nn = annotationElems(nn, nn.visitList(annotations, childbd));

        nn = (EnumConstantDecl) bd.leave(parent, node(), nn, childbd);

        return nn;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        super.prettyPrint(w, tr);
        superLang().prettyPrint(node(), w, tr);
    }

    @Override
    protected Declaration declaration() {
        EnumConstantDecl ecd = (EnumConstantDecl) this.node();
        return ecd.enumInstance();
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        EnumConstantDecl ecd = (EnumConstantDecl) this.node();
        ecd.enumInstance().setAnnotations(annotations);
    }

}
