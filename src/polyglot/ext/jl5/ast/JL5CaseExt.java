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
import polyglot.ast.Receiver;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ConstantChecker;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5CaseExt extends JL5TermExt implements JL5CaseOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Case node() {
        return (Case) super.node();
    }

    @Override
    public Case resolveCaseLabel(TypeChecker tc, Type switchType)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        JL5NodeFactory nf = (JL5NodeFactory) tc.nodeFactory();

        Case c = node();
        Expr expr = c.expr();

        if (expr == null) {
            return c;
        }
        else if (switchType.isClass() && !isNumericSwitchType(switchType, ts)) {
            // must be an enum...
            if (expr.type().isCanonical()) {
                // we have already resolved the expression
                EnumConstant ec = (EnumConstant) expr;
                return c.value(ec.enumInstance().ordinal());
            }
            else if (expr instanceof EnumConstant) {
                Field ec = (Field) expr;
                EnumConstant ext = (EnumConstant) ec;
                EnumInstance ei = ts.findEnumConstant(switchType.toReference(),
                                                      ec.name());
                ec = (Field) ext.enumInstance(ei);
                ec = (Field) ec.type(ei.type());
                return c.expr(ec).value(ei.ordinal());
            }
            else if (expr instanceof AmbExpr) {
                AmbExpr amb = (AmbExpr) expr;
                EnumInstance ei = ts.findEnumConstant(switchType.toReference(),
                                                      amb.name());
                Receiver r = nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                  switchType);
                Field e = nf.EnumConstant(expr.position(), r, amb.id());
                e = (Field) ((EnumConstant) e).enumInstance(ei);
                e = (Field) e.type(ei.type());
                return c.expr(e).value(ei.ordinal());
            }
            else {
                throw new InternalCompilerError("Unexpected case label "
                        + expr);
            }
        }

        // switch type is not a class.
        Case n = c;

        if (expr.isTypeChecked()) {
            // all good, nothing to do.
        }
        else if (expr instanceof AmbExpr) {
            AmbExpr amb = (AmbExpr) expr;
            //Disambiguate and typecheck
            Expr e = (Expr) tc.nodeFactory().disamb().disambiguate(amb,
                                                                   tc,
                                                                   expr.position(),
                                                                   null,
                                                                   amb.id());
            e = (Expr) e.visit(tc);
            n = n.expr(e);
        }
        else {
            // try type checking it
            n = c.expr((Expr) c.expr().visit(tc));
        }

        if (!tc.lang().constantValueSet(n.expr(), tc.lang())) {
            // Not ready yet; pass will get rerun.
            return n;
        }
        if (tc.lang().isConstant(n.expr(), tc.lang())) {
            Object o = tc.lang().constantValue(n.expr(), tc.lang());
            if (o instanceof Number && !(o instanceof Long)
                    && !(o instanceof Float) && !(o instanceof Double)) {
                return n.value(((Number) o).longValue());
            }
            else if (o instanceof Character) {
                return n.value(((Character) o).charValue());
            }
        }
        throw new SemanticException("Case label must be an integral constant or an unqualified enum value.",
                                    node().position());
    }

    public boolean isNumericSwitchType(Type switchType, JL5TypeSystem ts) {
        if (ts.Char().equals(switchType)
                || ts.wrapperClassOfPrimitive(ts.Char()).equals(switchType)) {
            return true;
        }
        if (ts.Byte().equals(switchType)
                || ts.wrapperClassOfPrimitive(ts.Byte()).equals(switchType)) {
            return true;
        }
        if (ts.Short().equals(switchType)
                || ts.wrapperClassOfPrimitive(ts.Short()).equals(switchType)) {
            return true;
        }
        if (ts.Int().equals(switchType)
                || ts.wrapperClassOfPrimitive(ts.Int()).equals(switchType)) {
            return true;
        }
        return false;
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        Case c = node();
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
        Case c = node();
        Expr expr = c.expr();
        if (expr == null || expr instanceof Lit) {
            return null;
        }
        // We will do type checking via the resolveCaseLabel method
        return c;
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        Case c = node();
        Expr expr = c.expr();
        if (expr == null) return c; // default case

        if (!cc.lang().constantValueSet(expr, cc.lang())) return c; // Not ready yet; pass will be rerun.

        if (expr instanceof EnumConstant) return c;

        return superLang().checkConstants(node(), cc);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        Case c = node();
        Expr expr = c.expr();
        if (expr == null) {
            w.write("default:");
        }
        else {
            w.write("case ");
            JL5TypeSystem ts = expr.type() == null
                    ? null : (JL5TypeSystem) expr.type().typeSystem();
            if (ts != null && expr.type().isReference()
                    && expr.type().isSubtype(ts.toRawType(ts.Enum()))) {
                // this is an enum
                Field f = (Field) expr;
                w.write(f.name());
            }
            else {
                print(expr, w, tr);
            }
            w.write(":");
        }
    }
}
