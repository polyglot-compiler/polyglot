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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ConstantChecker;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>Switch</code> is an immutable representation of a Java
 * <code>switch</code> statement.  Such a statement has an expression which
 * is evaluated to determine where to branch to, an a list of labels
 * and block statements which are conditionally evaluated.  One of the
 * labels, rather than having a constant expression, may be lablled
 * default.
 */
public class Switch_c extends Stmt_c implements Switch {
    protected Expr expr;
    protected List<SwitchElement> elements;

    public Switch_c(Position pos, Expr expr, List<SwitchElement> elements) {
        super(pos);
        assert (expr != null && elements != null);
        this.expr = expr;
        this.elements = ListUtil.copy(elements, true);
    }

    /** Get the expression to switch on. */
    @Override
    public Expr expr() {
        return this.expr;
    }

    /** Set the expression to switch on. */
    @Override
    public Switch expr(Expr expr) {
        Switch_c n = (Switch_c) copy();
        n.expr = expr;
        return n;
    }

    /** Get the switch elements of the statement. */
    @Override
    public List<SwitchElement> elements() {
        return Collections.unmodifiableList(this.elements);
    }

    /** Set the switch elements of the statement. */
    @Override
    public Switch elements(List<SwitchElement> elements) {
        Switch_c n = (Switch_c) copy();
        n.elements = ListUtil.copy(elements, true);
        return n;
    }

    /** Reconstruct the statement. */
    protected Switch_c reconstruct(Expr expr, List<SwitchElement> elements) {
        if (expr != this.expr
                || !CollectionUtil.equals(elements, this.elements)) {
            Switch_c n = (Switch_c) copy();
            n.expr = expr;
            n.elements = ListUtil.copy(elements, true);
            return n;
        }

        return this;
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = (Expr) visitChild(this.expr, v);
        List<SwitchElement> elements = visitList(this.elements, v);
        return reconstruct(expr, elements);
    }

    /** Type check the statement. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!ts.isImplicitCastValid(expr.type(), ts.Int())) {
            throw new SemanticException("Switch index must be an integer.",
                                        position());
        }

        return this;
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        Collection<Object> labels = new HashSet<Object>();

        // Check for duplicate labels.
        for (SwitchElement s : elements) {
            if (s instanceof Case) {
                Case c = (Case) s;
                Object key;
                String str;

                if (c.isDefault()) {
                    key = "default";
                    str = "default";
                }
                else if (!c.expr().constantValueSet()) {
                    // Constant not known yet; we'll try again later.
                    return this;
                }
                else if (c.expr().isConstant()) {
                    key = new Long(c.value());
                    str = c.expr().toString() + " (" + c.value() + ")";
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

    /** Write the statement to an output file. */
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
        List<Term> cases = new LinkedList<Term>();
        List<Integer> entry = new LinkedList<Integer>();
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
