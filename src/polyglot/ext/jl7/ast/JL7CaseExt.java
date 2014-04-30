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

import polyglot.ast.Case;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.J5Lang_c;
import polyglot.ext.jl5.ast.JL5CaseOps;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyglot.visit.ConstantChecker;
import polyglot.visit.TypeChecker;

public class JL7CaseExt extends JL7Ext implements JL5CaseOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Case resolveCaseLabel(TypeChecker tc, Type switchType)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        Case c = (Case) this.node();

        if (switchType.isClass() && ts.String().equals(switchType)) {
            return c;
        }
        return J5Lang_c.lang(pred()).resolveCaseLabel(c, tc, switchType);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Case c = (Case) this.node();
        TypeSystem ts = tc.typeSystem();
        if (c.expr() != null && ts.isSubtype(c.expr().type(), ts.String())) {
            // Strings are allowed!
            return c;
        }

        return superLang().typeCheck(this.node(), tc);
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        Case c = (Case) this.node();
        Expr expr = c.expr();
        if (expr == null) return c; // default case

        if (cc.lang().constantValueSet(expr, cc.lang())
                && cc.lang().isConstant(expr, cc.lang())
                && cc.lang().constantValue(expr, cc.lang()) instanceof String) {
            return c; // OK, it's a string.
        }

        return superLang().checkConstants(this.node(), cc);

    }
}
