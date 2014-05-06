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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ConstantChecker;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code Switch} is an immutable representation of a Java
 * {@code switch} statement.  Such a statement has an expression which
 * is evaluated to determine where to branch to, an a list of labels
 * and block statements which are conditionally evaluated.  One of the
 * labels, rather than having a constant expression, may be labelled
 * default.
 */
public class Switch_c extends Stmt_c implements Switch {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr expr;
    protected List<SwitchElement> elements;

//    @Deprecated
    public Switch_c(Position pos, Expr expr, List<SwitchElement> elements) {
        this(pos, expr, elements, null);
    }

    public Switch_c(Position pos, Expr expr, List<SwitchElement> elements,
            Ext ext) {
        super(pos, ext);
        assert (expr != null && elements != null);
        this.expr = expr;
        this.elements = ListUtil.copy(elements, true);
    }

    @Override
    public Expr expr() {
        return this.expr;
    }

    @Override
    public Switch expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends Switch_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    @Override
    public List<SwitchElement> elements() {
        return this.elements;
    }

    @Override
    public Switch elements(List<SwitchElement> elements) {
        return elements(this, elements);
    }

    protected <N extends Switch_c> N elements(N n, List<SwitchElement> elements) {
        if (CollectionUtil.equals(n.elements, elements)) return n;
        n = copyIfNeeded(n);
        n.elements = ListUtil.copy(elements, true);
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Switch_c> N reconstruct(N n, Expr expr,
            List<SwitchElement> elements) {
        n = expr(n, expr);
        n = elements(n, elements);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = visitChild(this.expr, v);
        List<SwitchElement> elements = visitList(this.elements, v);
        return reconstruct(this, expr, elements);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();
        Type type = expr.type();

        if (!ts.isImplicitCastValid(type, ts.Int())) {
            throw new SemanticException("Switch index must be an integer.",
                                        position());
        }

        // Check whether case constant expressions are assignable to the type of
        // expr.  See JLS 2nd Ed. | 14.10.
        for (SwitchElement se : elements) {
            if (se instanceof Case) {
                Case c = (Case) se;
                Expr cExpr = c.expr();
                if (cExpr != null
                        && !ts.isImplicitCastValid(cExpr.type(), type)
                        && !ts.typeEquals(cExpr.type(), type)
                        && !ts.numericConversionValid(type,
                                                      tc.lang()
                                                        .constantValue(cExpr,
                                                                       tc.lang()))) {
                    throw new SemanticException("Case constant \""
                                                        + cExpr
                                                        + "\" is not assignable to "
                                                        + type + ".",
                                                c.position());
                }
            }
        }

        return this;
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        Collection<Object> labels = new LinkedHashSet<>();

        // Check for duplicate labels.
        for (SwitchElement s : elements) {
            if (s instanceof Case) {
                Case c = (Case) s;
                Expr expr = c.expr();
                Object key;
                String str;

                if (c.isDefault()) {
                    key = "default";
                    str = "default";
                }
                else if (!cc.lang().constantValueSet(expr, cc.lang())) {
                    // Constant not known yet; we'll try again later.
                    return this;
                }
                else if (cc.lang().isConstant(expr, cc.lang())) {
                    key = new Long(c.value());
                    str = expr.toString() + " (" + c.value() + ")";
                }
                else {
                    continue;
                }

                if (labels.contains(key)) {
                    throw new SemanticException("Duplicate case label: " + str
                            + ".", c.position());
                }

                labels.add(key);
            }
        }

        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            return ts.Int();
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "switch (" + expr + ") { ... }";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("switch (");
        printBlock(expr, w, tr);
        w.write(") {");
        w.unifiedBreak(4);
        w.begin(0);

        boolean lastWasCase = false;
        boolean first = true;

        for (SwitchElement s : elements) {
            if (s instanceof Case) {
                if (lastWasCase)
                    w.unifiedBreak(0);
                else if (!first) w.unifiedBreak(0);
                printBlock(s, w, tr);
                lastWasCase = true;
            }
            else {
                w.unifiedBreak(4);
                print(s, w, tr);
                lastWasCase = false;
            }

            first = false;
        }

        w.end();
        w.unifiedBreak(0);
        w.write("}");
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        List<Term> cases = new LinkedList<>();
        List<Integer> entry = new LinkedList<>();
        boolean hasDefault = false;

        for (SwitchElement s : elements) {
            if (s instanceof Case) {
                cases.add(s);
                entry.add(new Integer(ENTRY));

                if (((Case) s).expr() == null) {
                    hasDefault = true;
                }
            }
        }

        // If there is no default case, add an edge to the end of the switch.
        if (!hasDefault) {
            cases.add(this);
            entry.add(new Integer(EXIT));
        }

        v.visitCFG(expr, FlowGraph.EDGE_KEY_OTHER, cases, entry);
        v.push(this).visitCFGList(elements, this, EXIT);

        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Switch(this.position, this.expr, this.elements);
    }

}
