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
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.util.CollectionUtil;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;

public abstract class JL5AnnotatedElementDel extends JL5Del {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node visitChildren(NodeVisitor v) {
        Node newN = super.visitChildren(v);
        JL5AnnotatedElementExt newext =
                (JL5AnnotatedElementExt) JL5Ext.ext(newN);

        List<AnnotationElem> annots =
                newN.visitList(newext.annotationElems(), v);

        if (!CollectionUtil.equals(annots, newext.annotationElems())) {
            // the annotations changed! Let's update the node.
            if (newN == this.node()) {
                // we need to create a copy.
                newN = (Node) newN.copy();
                newext = (JL5AnnotatedElementExt) JL5Ext.ext(newN);
            }
            else {
                // the call to super.visitChildren(v) already
                // created a copy of the node (and thus of its extension).
            }
            newext.annotations = annots;
        }
        return newN;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5AnnotatedElementExt ext =
                (JL5AnnotatedElementExt) JL5Ext.ext(this.node());

        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ts.checkDuplicateAnnotations(ext.annotationElems());
        return super.typeCheck(tc);

    }
}
