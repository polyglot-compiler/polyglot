/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.Collections;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>Cast</code> is an immutable representation of a casting
 * operation.  It consists of an <code>Expr</code> being cast and a
 * <code>TypeNode</code> being cast to.
 */
public class Cast_c extends Expr_c implements Cast {
    protected TypeNode castType;
    protected Expr expr;

    public Cast_c(Position pos, TypeNode castType, Expr expr) {
        super(pos);
        assert (castType != null && expr != null);
        this.castType = castType;
        this.expr = expr;
    }

    /** Get the precedence of the expression. */
    @Override
    public Precedence precedence() {
        return Precedence.CAST;
    }

    /** Get the cast type of the expression. */
    @Override
    public TypeNode castType() {
        return this.castType;
    }

    /** Set the cast type of the expression. */
    @Override
    public Cast castType(TypeNode castType) {
        Cast_c n = (Cast_c) copy();
        n.castType = castType;
        return n;
    }

    /** Get the expression being cast. */
    @Override
    public Expr expr() {
        return this.expr;
    }

    /** Set the expression being cast. */
    @Override
    public Cast expr(Expr expr) {
        Cast_c n = (Cast_c) copy();
        n.expr = expr;
        return n;
    }

    /** Reconstruct the expression. */
    protected Cast_c reconstruct(TypeNode castType, Expr expr) {
        if (castType != this.castType || expr != this.expr) {
            Cast_c n = (Cast_c) copy();
            n.castType = castType;
            n.expr = expr;
            return n;
        }

        return this;
    }

    /** Visit the children of the expression. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode castType = (TypeNode) visitChild(this.castType, v);
        Expr expr = (Expr) visitChild(this.expr, v);
        return reconstruct(castType, expr);
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!ts.isCastValid(expr.type(), castType.type())) {
            throw new SemanticException("Cannot cast the expression of type \""
                                                + expr.type() + "\" to type \""
                                                + castType.type() + "\".",
                                        position());
        }

        return type(castType.type());
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            if (castType.type().isReference()) {
                return ts.Object();
            }
            else if (castType.type().isNumeric()) {
                return ts.Double();
            }
            else if (castType.type().isBoolean()) {
                return ts.Boolean();
            }
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "(" + castType + ") " + expr;
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write("(");
        print(castType, w, tr);
        w.write(")");
        w.allowBreak(2, " ");
        printSubExpr(expr, w, tr);
        w.end();
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(expr, castType, ENTRY);
        v.visitCFG(castType, this, EXIT);
        return succs;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        if (expr.type().isReference()) {
            return Collections.singletonList((Type) ts.ClassCastException());
        }

        return Collections.<Type> emptyList();
    }

    @Override
    public boolean isConstant() {
        return expr.isConstant() && castType.type().isPrimitive();
    }

    @Override
    public Object constantValue() {
        Object v = expr.constantValue();

        if (v == null) {
            return null;
        }

        if (v instanceof Boolean) {
            if (castType.type().isBoolean()) return v;
        }

        if (v instanceof String) {
            TypeSystem ts = castType.type().typeSystem();
            if (castType.type().typeEquals(ts.String())) return v;
        }

        if (v instanceof Double) {
            double vv = ((Double) v).doubleValue();

            if (castType.type().isDouble()) return new Double(vv);
            if (castType.type().isFloat()) return new Float((float) vv);
            if (castType.type().isLong()) return new Long((long) vv);
            if (castType.type().isInt()) return new Integer((int) vv);
            if (castType.type().isChar()) return new Character((char) vv);
            if (castType.type().isShort()) return new Short((short) vv);
            if (castType.type().isByte()) return new Byte((byte) vv);
        }

        if (v instanceof Float) {
            float vv = ((Float) v).floatValue();

            if (castType.type().isDouble()) return new Double(vv);
            if (castType.type().isFloat()) return new Float(vv);
            if (castType.type().isLong()) return new Long((long) vv);
            if (castType.type().isInt()) return new Integer((int) vv);
            if (castType.type().isChar()) return new Character((char) vv);
            if (castType.type().isShort()) return new Short((short) vv);
            if (castType.type().isByte()) return new Byte((byte) vv);
        }

        if (v instanceof Number) {
            long vv = ((Number) v).longValue();

            if (castType.type().isDouble()) return new Double(vv);
            if (castType.type().isFloat()) return new Float(vv);
            if (castType.type().isLong()) return new Long(vv);
            if (castType.type().isInt()) return new Integer((int) vv);
            if (castType.type().isChar()) return new Character((char) vv);
            if (castType.type().isShort()) return new Short((short) vv);
            if (castType.type().isByte()) return new Byte((byte) vv);
        }

        if (v instanceof Character) {
            char vv = ((Character) v).charValue();

            if (castType.type().isDouble()) return new Double(vv);
            if (castType.type().isFloat()) return new Float(vv);
            if (castType.type().isLong()) return new Long(vv);
            if (castType.type().isInt()) return new Integer(vv);
            if (castType.type().isChar()) return new Character(vv);
            if (castType.type().isShort()) return new Short((short) vv);
            if (castType.type().isByte()) return new Byte((byte) vv);
        }

        // not a constant
        return null;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Cast(this.position, this.castType, this.expr);
    }

}
