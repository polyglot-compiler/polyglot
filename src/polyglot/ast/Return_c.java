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

import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.FunctionInstance;
import polyglot.types.InitializerInstance;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>Return</code> represents a <code>return</code> statement in Java.
 * It may or may not return a value.  If not <code>expr()</code> should return
 * null.
 */
public class Return_c extends Stmt_c implements Return {
    protected Expr expr;

    public Return_c(Position pos, Expr expr) {
        super(pos);
        assert (true); // expr may be null
        this.expr = expr;
    }

    /** Get the expression to return, or null. */
    @Override
    public Expr expr() {
        return this.expr;
    }

    /** Set the expression to return, or null. */
    @Override
    public Return expr(Expr expr) {
        Return_c n = (Return_c) copy();
        n.expr = expr;
        return n;
    }

    /** Reconstruct the statement. */
    protected Return_c reconstruct(Expr expr) {
        if (expr != this.expr) {
            Return_c n = (Return_c) copy();
            n.expr = expr;
            return n;
        }

        return this;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = (Expr) visitChild(this.expr, v);
        return reconstruct(expr);
    }

    /** Type check the statement. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();
        Context c = tc.context();

        CodeInstance ci = c.currentCode();

        if (ci instanceof InitializerInstance) {
            throw new SemanticException("Cannot return from an initializer block.",
                                        position());
        }

        if (ci instanceof ConstructorInstance) {
            if (expr != null) {
                throw new SemanticException("Cannot return a value from " + ci
                        + ".", position());
            }

            return this;
        }

        if (ci instanceof FunctionInstance) {
            FunctionInstance fi = (FunctionInstance) ci;

            if (fi.returnType().isVoid()) {
                if (expr != null) {
                    throw new SemanticException("Cannot return a value from "
                            + fi + ".", position());
                }
                else {
                    return this;
                }
            }
            else if (expr == null) {
                throw new SemanticException("Must return a value from " + fi
                        + ".", position());
            }

            if (ts.isImplicitCastValid(expr.type(), fi.returnType())) {
                return this;
            }

            if (ts.numericConversionValid(fi.returnType(), expr.constantValue())) {
                return this;
            }

            throw new SemanticException("Cannot return expression of type "
                    + expr.type() + " from " + fi + ".", expr.position());
        }

        throw new InternalCompilerError("Unrecognized code type.");
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == expr) {
            Context c = av.context();
            CodeInstance ci = c.currentCode();

            if (ci instanceof MethodInstance) {
                MethodInstance mi = (MethodInstance) ci;

                TypeSystem ts = av.typeSystem();

                // If expr is an integral constant, we can relax the expected
                // type to the type of the constant.
                if (ts.numericConversionValid(mi.returnType(),
                                              child.constantValue())) {
                    return child.type();
                }
                else {
                    return mi.returnType();
                }
            }
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "return" + (expr != null ? " " + expr : "") + ";";
    }

    /** Write the statement to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("return");
        if (expr != null) {
            w.write(" ");
            print(expr, w, tr);
        }
        w.write(";");
    }

    @Override
    public Term firstChild() {
        if (expr != null) return expr;
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (expr != null) {
            v.visitCFG(expr, this, EXIT);
        }

        v.visitReturn(this);
        return Collections.<T> emptyList();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Return(this.position, this.expr);
    }

}
