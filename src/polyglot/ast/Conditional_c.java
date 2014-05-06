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
 * A {@code Conditional} is a representation of a Java ternary
 * expression.  That is, {@code (cond ? consequent : alternative)}.
 */
public class Conditional_c extends Expr_c implements Conditional {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr cond;
    protected Expr consequent;
    protected Expr alternative;

//    @Deprecated
    public Conditional_c(Position pos, Expr cond, Expr consequent,
            Expr alternative) {
        this(pos, cond, consequent, alternative, null);
    }

    public Conditional_c(Position pos, Expr cond, Expr consequent,
            Expr alternative, Ext ext) {
        super(pos, ext);
        assert (cond != null && consequent != null && alternative != null);
        this.cond = cond;
        this.consequent = consequent;
        this.alternative = alternative;
    }

    @Override
    public Precedence precedence() {
        return Precedence.CONDITIONAL;
    }

    @Override
    public Expr cond() {
        return this.cond;
    }

    @Override
    public Conditional cond(Expr cond) {
        return cond(this, cond);
    }

    protected <N extends Conditional_c> N cond(N n, Expr cond) {
        if (n.cond == cond) return n;
        n = copyIfNeeded(n);
        n.cond = cond;
        return n;
    }

    @Override
    public Expr consequent() {
        return this.consequent;
    }

    @Override
    public Conditional consequent(Expr consequent) {
        return consequent(this, consequent);
    }

    protected <N extends Conditional_c> N consequent(N n, Expr consequent) {
        if (n.consequent == consequent) return n;
        n = copyIfNeeded(n);
        n.consequent = consequent;
        return n;
    }

    @Override
    public Expr alternative() {
        return this.alternative;
    }

    @Override
    public Conditional alternative(Expr alternative) {
        return alternative(this, alternative);
    }

    protected <N extends Conditional_c> N alternative(N n, Expr alternative) {
        if (n.alternative == alternative) return n;
        n = copyIfNeeded(n);
        n.alternative = alternative;
        return n;
    }

    /** Reconstruct the expression. */
    protected <N extends Conditional_c> N reconstruct(N n, Expr cond,
            Expr consequent, Expr alternative) {
        n = cond(n, cond);
        n = consequent(n, consequent);
        n = alternative(n, alternative);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr cond = visitChild(this.cond, v);
        Expr consequent = visitChild(this.consequent, v);
        Expr alternative = visitChild(this.alternative, v);
        return reconstruct(this, cond, consequent, alternative);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        Expr e1 = consequent;
        Expr e2 = alternative;
        Type t1 = e1.type();
        Type t2 = e2.type();

        if (!ts.typeEquals(cond.type(), ts.Boolean())) {
            throw new SemanticException("Condition of ternary expression must be of type boolean.",
                                        cond.position());
        }

        // From the JLS, section:
        // If the second and third operands have the same type (which may be
        // the null type), then that is the type of the conditional expression.
        if (ts.typeEquals(t1, t2)) {
            return type(t1);
        }

        // Otherwise, if the second and third operands have numeric type, then
        // there are several cases:
        if (t1.isNumeric() && t2.isNumeric()) {
            // - If one of the operands is of type byte and the other is of
            // type short, then the type of the conditional expression is
            // short.
            if (t1.isByte() && t2.isShort() || t1.isShort() && t2.isByte()) {
                return type(ts.Short());
            }

            // - If one of the operands is of type T where T is byte, short, or
            // char, and the other operand is a constant expression of type int
            // whose value is representable in type T, then the type of the
            // conditional expression is T.

            if (t1.isIntOrLess()
                    && t2.isInt()
                    && ts.numericConversionValid(t1,
                                                 tc.lang()
                                                   .constantValue(e2, tc.lang()))) {
                return type(t1);
            }

            if (t2.isIntOrLess()
                    && t1.isInt()
                    && ts.numericConversionValid(t2,
                                                 tc.lang()
                                                   .constantValue(e1, tc.lang()))) {
                return type(t2);
            }

            // - Otherwise, binary numeric promotion (Sec. 5.6.2) is applied to the
            // operand types, and the type of the conditional expression is the
            // promoted type of the second and third operands. Note that binary
            // numeric promotion performs value set conversion (Sec. 5.1.8).
            return type(ts.promote(t1, t2));
        }

        // If one of the second and third operands is of the null type and the
        // type of the other is a reference type, then the type of the
        // conditional expression is that reference type.
        if (t1.isNull() && t2.isReference()) return type(t2);
        if (t2.isNull() && t1.isReference()) return type(t1);

        // If the second and third operands are of different reference types,
        // then it must be possible to convert one of the types to the other
        // type (call this latter type T) by assignment conversion (Sec. 5.2); the
        // type of the conditional expression is T. It is a compile-time error
        // if neither type is assignment compatible with the other type.

        if (t1.isReference() && t2.isReference()) {
            if (ts.isImplicitCastValid(t1, t2)) {
                return type(t2);
            }
            if (ts.isImplicitCastValid(t2, t1)) {
                return type(t1);
            }
        }

        throw new SemanticException("Could not determine type of ternary conditional expression; cannot assign "
                                            + t1
                                            + " to "
                                            + t2
                                            + " or vice versa.",
                                    position());
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == cond) {
            return ts.Boolean();
        }

        if (child == consequent || child == alternative) {
            return type();
        }

        return child.type();
    }

    @Override
    public String toString() {
        return cond + " ? " + consequent + " : " + alternative;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printSubExpr(cond, false, w, tr);
        w.unifiedBreak(2);
        w.write("? ");
        printSubExpr(consequent, false, w, tr);
        w.unifiedBreak(2);
        w.write(": ");
        printSubExpr(alternative, false, w, tr);
    }

    @Override
    public Term firstChild() {
        return cond;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        boolean isBoolean = type.isBoolean();
        if (v.lang().isConstant(cond, v.lang())) {
            // the condition is a constant expression.
            // That means that one branch is dead code
            boolean condConstantValue =
                    ((Boolean) v.lang().constantValue(cond, v.lang())).booleanValue();
            if (condConstantValue) {
                // Condition is constantly true, only the consequent will be executed
                v.visitCFG(cond, FlowGraph.EDGE_KEY_TRUE, consequent, ENTRY);
                if (isBoolean)
                    v.visitCFG(consequent,
                               FlowGraph.EDGE_KEY_TRUE,
                               this,
                               EXIT,
                               FlowGraph.EDGE_KEY_FALSE,
                               this,
                               EXIT);
                else v.visitCFG(consequent, this, EXIT);
            }
            else {
                // Condition is constantly false, only the alternative will be executed
                v.visitCFG(cond, FlowGraph.EDGE_KEY_FALSE, alternative, ENTRY);
                if (isBoolean)
                    v.visitCFG(alternative,
                               FlowGraph.EDGE_KEY_TRUE,
                               this,
                               EXIT,
                               FlowGraph.EDGE_KEY_FALSE,
                               this,
                               EXIT);
                else v.visitCFG(alternative, this, EXIT);
            }
        }
        else {
            // not a constant condition
            v.visitCFG(cond,
                       FlowGraph.EDGE_KEY_TRUE,
                       consequent,
                       ENTRY,
                       FlowGraph.EDGE_KEY_FALSE,
                       alternative,
                       ENTRY);
            if (isBoolean) {
                v.visitCFG(consequent,
                           FlowGraph.EDGE_KEY_TRUE,
                           this,
                           EXIT,
                           FlowGraph.EDGE_KEY_FALSE,
                           this,
                           EXIT);
                v.visitCFG(alternative,
                           FlowGraph.EDGE_KEY_TRUE,
                           this,
                           EXIT,
                           FlowGraph.EDGE_KEY_FALSE,
                           this,
                           EXIT);
            }
            else {
                v.visitCFG(consequent, this, EXIT);
                v.visitCFG(alternative, this, EXIT);
            }
        }

        return succs;
    }

    @Override
    public boolean isConstant(Lang lang) {
        return lang.isConstant(cond, lang) && lang.isConstant(consequent, lang)
                && lang.isConstant(alternative, lang);
    }

    @Override
    public Object constantValue(Lang lang) {
        Object cond_ = lang.constantValue(cond, lang);
        Object then_ = lang.constantValue(consequent, lang);
        Object else_ = lang.constantValue(alternative, lang);

        if (cond_ instanceof Boolean && then_ != null && else_ != null) {
            boolean c = ((Boolean) cond_).booleanValue();
            if (c) {
                return then_;
            }
            else {
                return else_;
            }
        }

        return null;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Conditional(this.position,
                              this.cond,
                              this.consequent,
                              this.alternative);
    }

}
