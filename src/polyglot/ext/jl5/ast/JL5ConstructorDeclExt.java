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
import polyglot.ast.Node;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ConstructorInstance;
import polyglot.types.Declaration;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5ConstructorDeclExt extends JL5ProcedureDeclExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5ConstructorDeclExt() {
        this(null, null);
    }

    public JL5ConstructorDeclExt(List<ParamTypeNode> typeParams,
            List<AnnotationElem> annotations) {
        super(typeParams, annotations);
    }

    @Override
    protected Declaration declaration() {
        ConstructorDecl cd = (ConstructorDecl) this.node();
        return cd.constructorInstance();
    }

    @Override
    protected Node buildTypesFinish(JL5TypeSystem ts, ParsedClassType ct,
            Flags flags, List<? extends Type> formalTypes,
            List<? extends Type> throwTypes, List<TypeVariable> typeParams) {
        ConstructorDecl cd = (ConstructorDecl) this.node();
        ConstructorInstance ci =
                ts.constructorInstance(cd.position(),
                                       ct,
                                       flags,
                                       formalTypes,
                                       throwTypes,
                                       typeParams);
        ct.addConstructor(ci);

        return cd.constructorInstance(ci).flags(flags);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ConstructorDecl cd = (ConstructorDecl) super.typeCheck(tc);
        return superLang().typeCheck(cd, tc);
    }

    @Override
    protected void prettyPrintName(CodeWriter w, PrettyPrinter pp) {
        ConstructorDecl n = (ConstructorDecl) this.node();
        pp.print(n, n.id(), w);
    }
}
