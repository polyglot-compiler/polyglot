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
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An {@code Instanceof} is an immutable representation of
 * the use of the {@code instanceof} operator.
 */
public class Instanceof_c extends Expr_c implements Instanceof {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr expr;
    protected TypeNode compareType;

//    @Deprecated
    public Instanceof_c(Position pos, Expr expr, TypeNode compareType) {
        this(pos, expr, compareType, null);
    }

    public Instanceof_c(Position pos, Expr expr, TypeNode compareType, Ext ext) {
        super(pos, ext);
        assert (expr != null && compareType != null);
        this.expr = expr;
        this.compareType = compareType;
    }

    @Override
    public Precedence precedence() {
        return Precedence.INSTANCEOF;
    }

    @Override
    public Expr expr() {
        return this.expr;
    }

    @Override
    public Instanceof expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends Instanceof_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    @Override
    public TypeNode compareType() {
        return this.compareType;
    }

    @Override
    public Instanceof compareType(TypeNode compareType) {
        return compareType(this, compareType);
    }

    protected <N extends Instanceof_c> N compareType(N n, TypeNode compareType) {
        if (n.compareType == compareType) return n;
        n = copyIfNeeded(n);
        n.compareType = compareType;
        return n;
    }

    /** Reconstruct the expression. */
    protected <N extends Instanceof_c> N reconstruct(N n, Expr expr,
            TypeNode compareType) {
        n = expr(n, expr);
        n = compareType(n, compareType);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = visitChild(this.expr, v);
        TypeNode compareType = visitChild(this.compareType, v);
        return reconstruct(this, expr, compareType);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!compareType.type().isReference()) {
            throw new SemanticException("Type operand " + compareType.type()
                    + " must be a reference type.", compareType.position());
        }

        if (!ts.isCastValid(expr.type(), compareType.type())) {
            throw new SemanticException("Expression operand type "
                    + expr.type() + " is incompatible with type operand "
                    + compareType.type() + ".", expr.position());
        }

        return type(ts.Boolean());
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            return ts.Object();
        }

        return child.type();
    }

    @Override
    public String toString() {
        return expr + " instanceof " + compareType;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printSubExpr(expr, w, tr);
        w.write(" instanceof ");
        print(compareType, w, tr);
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(expr, compareType, ENTRY);
        v.visitCFG(compareType, this, EXIT);
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Instanceof(this.position, this.expr, this.compareType);
    }

}
