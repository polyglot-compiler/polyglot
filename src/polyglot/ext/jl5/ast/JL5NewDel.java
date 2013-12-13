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

import polyglot.ast.New;
import polyglot.ast.NewOps;
import polyglot.ast.TypeNode;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5NewDel extends JL5Del implements NewOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public TypeNode findQualifiedTypeNode(AmbiguityRemover ar, ClassType outer,
            TypeNode objectType) throws SemanticException {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());
        return ext.findQualifiedTypeNode(ar, outer, objectType);
    }

    @Override
    public New findQualifier(AmbiguityRemover ar, ClassType ct)
            throws SemanticException {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());
        return ext.findQualifier(ar, ct);
    }

    @Override
    public ClassType findEnclosingClass(Context c, ClassType ct) {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());
        return ext.findEnclosingClass(c, ct);
    }

    @Override
    public void typeCheckFlags(TypeChecker tc) throws SemanticException {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());
        ext.typeCheckFlags(tc);
    }

    @Override
    public void typeCheckNested(TypeChecker tc) throws SemanticException {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());
        ext.typeCheckNested(tc);
    }

    @Override
    public void printQualifier(CodeWriter w, PrettyPrinter tr) {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());
        ext.printQualifier(w, tr);
    }

    @Override
    public void printArgs(CodeWriter w, PrettyPrinter tr) {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());
        ext.printArgs(w, tr);
    }

    @Override
    public void printBody(CodeWriter w, PrettyPrinter tr) {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());
        ext.printBody(w, tr);
    }

}
