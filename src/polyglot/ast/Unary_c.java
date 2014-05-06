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

package polyglot.ast;

import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code Unary} represents a Java unary expression, an
 * immutable pair of an expression and an operator.
 */
public class Unary_c extends Expr_c implements Unary {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Unary.Operator op;
    protected Expr expr;

//    @Deprecated
    public Unary_c(Position pos, Unary.Operator op, Expr expr) {
        this(pos, op, expr, null);
    }

    public Unary_c(Position pos, Unary.Operator op, Expr expr, Ext ext) {
        super(pos, ext);
        assert (op != null && expr != null);
        this.op = op;
        this.expr = expr;
    }

    @Override
    public Precedence precedence() {
        return Precedence.UNARY;
    }

    @Override
    public Expr expr() {
        return this.expr;
    }

    @Override
    public Unary expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends Unary_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    @Override
    public Unary.Operator operator() {
        return this.op;
    }

    @Override
    public Unary operator(Unary.Operator op) {
        return operator(this, op);
    }

    protected <N extends Unary_c> N operator(N n, Unary.Operator op) {
        if (n.op == op) return n;
        n = copyIfNeeded(n);
        n.op = op;
        return n;
    }

    /** Reconstruct the expression. */
    protected <N extends Unary_c> N reconstruct(N n, Expr expr) {
        n = expr(n, expr);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = visitChild(this.expr, v);
        return reconstruct(this, expr);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (op == POST_INC || op == POST_DEC || op == PRE_INC || op == PRE_DEC) {

            if (!expr.type().isNumeric()) {
                throw new SemanticException("Operand of " + op
                        + " operator must be numeric.", expr.position());
            }

            if (!(expr instanceof Variable)) {
                throw new SemanticException("Operand of " + op
                        + " operator must be a variable.", expr.position());
            }

            if (((Variable) expr).flags().isFinal()) {
                throw new SemanticException("Operand of "
                                                    + op
                                                    + " operator must be a non-final variable.",
                                            expr.position());
            }

            return type(expr.type());
        }

        if (op == BIT_NOT) {
            if (!ts.isImplicitCastValid(expr.type(), ts.Long())) {
                throw new SemanticException("Operand of " + op
                        + " operator must be numeric.", expr.position());
            }

            return type(ts.promote(expr.type()));
        }

        if (op == NEG || op == POS) {
            if (!expr.type().isNumeric()) {
                throw new SemanticException("Operand of " + op
                        + " operator must be numeric.", expr.position());
            }

            return type(ts.promote(expr.type()));
        }

        if (op == NOT) {
            if (!expr.type().isBoolean()) {
                throw new SemanticException("Operand of " + op
                        + " operator must be boolean.", expr.position());
            }

            return type(expr.type());
        }

        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        try {
            if (child == expr) {
                if (op == POST_INC || op == POST_DEC || op == PRE_INC
                        || op == PRE_DEC) {

                    if (ts.isImplicitCastValid(child.type(), av.toType())) {
                        return ts.promote(child.type());
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == NEG || op == POS) {
                    if (ts.isImplicitCastValid(child.type(), av.toType())) {
                        return ts.promote(child.type());
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == BIT_NOT) {
                    if (ts.isImplicitCastValid(child.type(), av.toType())) {
                        return ts.promote(child.type());
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == NOT) {
                    return ts.Boolean();
                }
            }
        }
        catch (SemanticException e) {
        }

        return child.type();
    }

    @Override
    public String toString() {
        if (op == NEG && expr instanceof IntLit && ((IntLit) expr).boundary()) {
            return op.toString() + ((IntLit) expr).positiveToString();
        }
        else if (op.isPrefix()) {
            return op.toString() + expr.toString();
        }
        else {
            return expr.toString() + op.toString();
        }
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (op == NEG && expr instanceof IntLit && ((IntLit) expr).boundary()) {
            w.write(op.toString());
            w.write(((IntLit) expr).positiveToString());
        }
        else if (op.isPrefix()) {
            w.write(op.toString());
            printSubExpr(expr, false, w, tr);
        }
        else {
            printSubExpr(expr, false, w, tr);
            w.write(op.toString());
        }
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (expr.type().isBoolean()) {
            v.visitCFG(expr,
                       FlowGraph.EDGE_KEY_TRUE,
                       this,
                       EXIT,
                       FlowGraph.EDGE_KEY_FALSE,
                       this,
                       EXIT);
        }
        else {
            v.visitCFG(expr, this, EXIT);
        }

        return succs;
    }

    @Override
    public boolean constantValueSet(Lang lang) {
        return lang.constantValueSet(expr, lang);
    }

    @Override
    public boolean isConstant(Lang lang) {
        if (op == POST_INC || op == POST_DEC || op == PRE_INC || op == PRE_DEC) {
            return false;
        }
        return lang.isConstant(expr, lang);
    }

    @Override
    public Object constantValue(Lang lang) {
        if (!lang.isConstant(this, lang)) {
            return null;
        }

        Object v = lang.constantValue(expr, lang);

        if (v instanceof Boolean) {
            boolean vv = ((Boolean) v).booleanValue();
            if (op == NOT) return Boolean.valueOf(!vv);
        }
        if (v instanceof Double) {
            double vv = ((Double) v).doubleValue();
            if (op == POS) return new Double(+vv);
            if (op == NEG) return new Double(-vv);
        }
        if (v instanceof Float) {
            float vv = ((Float) v).floatValue();
            if (op == POS) return new Float(+vv);
            if (op == NEG) return new Float(-vv);
        }
        if (v instanceof Long) {
            long vv = ((Long) v).longValue();
            if (op == BIT_NOT) return new Long(~vv);
            if (op == POS) return new Long(+vv);
            if (op == NEG) return new Long(-vv);
        }
        if (v instanceof Integer) {
            int vv = ((Integer) v).intValue();
            if (op == BIT_NOT) return new Integer(~vv);
            if (op == POS) return new Integer(+vv);
            if (op == NEG) return new Integer(-vv);
        }
        if (v instanceof Character) {
            char vv = ((Character) v).charValue();
            if (op == BIT_NOT) return new Integer(~vv);
            if (op == POS) return new Integer(+vv);
            if (op == NEG) return new Integer(-vv);
        }
        if (v instanceof Short) {
            short vv = ((Short) v).shortValue();
            if (op == BIT_NOT) return new Integer(~vv);
            if (op == POS) return new Integer(+vv);
            if (op == NEG) return new Integer(-vv);
        }
        if (v instanceof Byte) {
            byte vv = ((Byte) v).byteValue();
            if (op == BIT_NOT) return new Integer(~vv);
            if (op == POS) return new Integer(+vv);
            if (op == NEG) return new Integer(-vv);
        }

        // not a constant
        return null;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Unary(this.position, this.op, this.expr);
    }

}
