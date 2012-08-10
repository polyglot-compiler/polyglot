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
 * An <code>Instanceof</code> is an immutable representation of
 * the use of the <code>instanceof</code> operator.
 */
public class Instanceof_c extends Expr_c implements Instanceof {
    protected Expr expr;
    protected TypeNode compareType;

    public Instanceof_c(Position pos, Expr expr, TypeNode compareType) {
        super(pos);
        assert (expr != null && compareType != null);
        this.expr = expr;
        this.compareType = compareType;
    }

    /** Get the precedence of the expression. */
    @Override
    public Precedence precedence() {
        return Precedence.INSTANCEOF;
    }

    /** Get the expression to be tested. */
    @Override
    public Expr expr() {
        return this.expr;
    }

    /** Set the expression to be tested. */
    @Override
    public Instanceof expr(Expr expr) {
        Instanceof_c n = (Instanceof_c) copy();
        n.expr = expr;
        return n;
    }

    /** Get the type to be compared against. */
    @Override
    public TypeNode compareType() {
        return this.compareType;
    }

    /** Set the type to be compared against. */
    @Override
    public Instanceof compareType(TypeNode compareType) {
        Instanceof_c n = (Instanceof_c) copy();
        n.compareType = compareType;
        return n;
    }

    /** Reconstruct the expression. */
    protected Instanceof_c reconstruct(Expr expr, TypeNode compareType) {
        if (expr != this.expr || compareType != this.compareType) {
            Instanceof_c n = (Instanceof_c) copy();
            n.expr = expr;
            n.compareType = compareType;
            return n;
        }

        return this;
    }

    /** Visit the children of the expression. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = (Expr) visitChild(this.expr, v);
        TypeNode compareType = (TypeNode) visitChild(this.compareType, v);
        return reconstruct(expr, compareType);
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!compareType.type().isReference()) {
            throw new SemanticException("Type operand " + compareType.type()
                    + " must be a reference type.", compareType.position());
        }

        if (!ts.isCastValid(expr.type(), compareType.type())) {
            throw new SemanticException("Expression operand type "
                    + expr.type() + " incompatible with type operand "
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

    /** Write the expression to an output file. */
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
