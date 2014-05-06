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

import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A {@code Branch} is an immutable representation of a branch
 * statment in Java (a break or continue).
 */
public class Branch_c extends Stmt_c implements Branch {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Branch.Kind kind;
    protected Id label;

//    @Deprecated
    public Branch_c(Position pos, Branch.Kind kind, Id label) {
        this(pos, kind, label, null);
    }

    public Branch_c(Position pos, Branch.Kind kind, Id label, Ext ext) {
        super(pos, ext);
        assert (kind != null); // label may be null
        this.kind = kind;
        this.label = label;
    }

    @Override
    public Branch.Kind kind() {
        return this.kind;
    }

    @Override
    public Branch kind(Branch.Kind kind) {
        return kind(this, kind);
    }

    protected <N extends Branch_c> N kind(N n, Branch.Kind kind) {
        if (n.kind == kind) return n;
        n = copyIfNeeded(n);
        n.kind = kind;
        return n;
    }

    @Override
    public Id labelNode() {
        return this.label;
    }

    @Override
    public Branch labelNode(Id label) {
        return labelNode(this, label);
    }

    protected <N extends Branch_c> N labelNode(N n, Id label) {
        if (n.label == label) return n;
        n = copyIfNeeded(n);
        n.label = label;
        return n;
    }

    @Override
    public String label() {
        return this.label != null ? this.label.id() : null;
    }

    @Override
    public Branch label(String label) {
        return labelNode(this.label.id(label));
    }

    /** Reconstruct the expression. */
    protected <N extends Branch_c> N reconstruct(N n, Id label) {
        n = labelNode(n, label);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id label = visitChild(this.label, v);
        return reconstruct(this, label);
    }

    @Override
    public String toString() {
        return kind.toString() + (label != null ? " " + label.toString() : "");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(kind.toString());
        if (label != null) {
            w.write(" " + label);
        }
        w.write(";");
    }

    @Override
    public Term firstChild() {
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitBranchTarget(this);
        return Collections.<T> emptyList();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Branch(this.position, this.kind, this.label);
    }

}
