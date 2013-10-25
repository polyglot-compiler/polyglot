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

import polyglot.ast.AmbExpr;
import polyglot.ast.Case;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Lit;
import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ConstantChecker;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5CaseDel extends JL5Del {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        Case c = (Case) this.node();
        Expr expr = c.expr();
        // We can't disambiguate unqualified names until the switch expression
        // is typed.
        if (expr instanceof AmbExpr) {
            return c;
        }
        else {
            return null;
        }
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        Case c = (Case) this.node();
        Expr expr = c.expr();
        if (expr == null || expr instanceof Lit) {
            return null;
        }
        // We will do type checking vis the resolveCaseLabel method
        return c;
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        Case c = (Case) this.node();
        Expr expr = c.expr();
        if (expr == null) return c; // default case

        if (!expr.constantValueSet()) return c; // Not ready yet; pass will be rerun.

        if (expr instanceof EnumConstant) return c;

        return super.checkConstants(cc);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        Case c = (Case) this.node();
        Expr expr = c.expr();
        if (expr == null) {
            w.write("default:");
        }
        else {
            w.write("case ");
            JL5TypeSystem ts =
                    expr.type() == null ? null
                            : (JL5TypeSystem) expr.type().typeSystem();
            if (ts != null && expr.type().isReference()
                    && expr.type().isSubtype(ts.toRawType(ts.Enum()))) {
                // this is an enum	            
                Field f = (Field) expr;
                w.write(f.name());
            }
            else {
                ((Node_c) c).print(expr, w, tr);
            }
            w.write(":");
        }
    }

}
