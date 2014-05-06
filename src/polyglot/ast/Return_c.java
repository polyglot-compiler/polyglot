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
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code Return} represents a {@code return} statement in Java.
 * It may or may not return a value.  If not {@code expr()} should return
 * null.
 */
public class Return_c extends Stmt_c implements Return {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr expr;

//    @Deprecated
    public Return_c(Position pos, Expr expr) {
        this(pos, expr, null);
    }

    public Return_c(Position pos, Expr expr, Ext ext) {
        super(pos, ext);
        assert (true); // expr may be null
        this.expr = expr;
    }

    @Override
    public Expr expr() {
        return this.expr;
    }

    @Override
    public Return expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends Return_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Return_c> N reconstruct(N n, Expr expr) {
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

            if (ts.numericConversionValid(fi.returnType(),
                                          tc.lang().constantValue(expr,
                                                                  tc.lang()))) {
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
                // expected type of the return is the return type of the method.
                return mi.returnType();
            }
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "return" + (expr != null ? " " + expr : "") + ";";
    }

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
