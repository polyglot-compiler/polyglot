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

import polyglot.ast.Expr;
import polyglot.ast.Switch;
import polyglot.ext.jl5.ast.J5Lang_c;
import polyglot.ext.jl5.ast.JL5SwitchOps;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;

public class JL7SwitchExt extends JL7Ext implements JL5SwitchOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Switch node() {
        return (Switch) super.node();
    }

    @Override
    public boolean isAcceptableSwitchType(Type type) {
        JL5TypeSystem ts = (JL5TypeSystem) type.typeSystem();
        if (ts.String().equals(type)) {
            return true;
        }
        return J5Lang_c.lang(pred()).isAcceptableSwitchType(this.node(), type);
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        Switch n = node();
        TypeSystem ts = av.typeSystem();

        if (child == n.expr() && n.expr().type().isSubtype(ts.String())) return ts.String();

        return super.childExpectedType(child, av);
    }
}
