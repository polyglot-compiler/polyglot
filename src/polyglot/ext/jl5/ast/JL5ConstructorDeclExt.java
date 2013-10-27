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

import polyglot.ast.ConstructorDecl;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.types.Declaration;
import polyglot.util.SerialVersionUID;

public class JL5ConstructorDeclExt extends JL5AnnotatedElementExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<ParamTypeNode> typeParams;

    public List<ParamTypeNode> typeParams() {
        return this.typeParams;
    }

    public ConstructorDecl typeParams(List<ParamTypeNode> typeParams) {

        ConstructorDecl n = (ConstructorDecl) this.node().copy();
        JL5ConstructorDeclExt ext = (JL5ConstructorDeclExt) JL5Ext.ext(n);
        ext.typeParams = typeParams;
        return n;
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        ConstructorDecl cd = (ConstructorDecl) this.node();
        JL5ConstructorInstance ci =
                (JL5ConstructorInstance) cd.constructorInstance();
        ci.setAnnotations(annotations);
    }

    @Override
    protected Declaration declaration() {
        ConstructorDecl cd = (ConstructorDecl) this.node();
        return cd.constructorInstance();
    }

}
