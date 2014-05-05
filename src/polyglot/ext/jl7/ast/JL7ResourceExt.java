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
package polyglot.ext.jl7.ast;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

public class JL7ResourceExt extends JL7Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public LocalDecl node() {
        return (LocalDecl) super.node();
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        LocalDecl n = this.node();
        Type declType = n.declType();
        JL7TypeSystem ts = (JL7TypeSystem) tc.typeSystem();
        if (!ts.isSubtype(declType, ts.AutoCloseable())) {
            // JLS SE 7 | 14.20.3
            // The type of a variable declared in a ResourceSpecification must be a
            // subtype of AutoCloseable, or a compile-time error occurs. 
            throw new SemanticException("The resource type " + declType
                    + " does not implement java.lang.AutoCloseable");
        }
        return superLang().typeCheck(this.node(), tc);
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        List<Type> l = new LinkedList<>();

        LocalDecl n = this.node();
        ClassType declType = n.declType().toClass();
        JL7TypeSystem jl7ts = (JL7TypeSystem) ts;
        try {
            MethodInstance mi =
                    ts.findMethod(declType,
                                  "close",
                                  Collections.<Type> emptyList(),
                                  jl7ts.AutoCloseable(),
                                  true);
            // The resource may throw exceptions declared by close().
            l.addAll(mi.throwTypes());
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("Unexpected SemanticException: "
                    + e);
        }

        l.addAll(superLang().throwTypes(n, ts));

        return l;
    }
}
