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

import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A <code>Branch</code> is an immutable representation of a branch
 * statment in Java (a break or continue).
 */
public class Branch_c extends Stmt_c implements Branch {
    protected Branch.Kind kind;
    protected Id label;

    public Branch_c(Position pos, Branch.Kind kind, Id label) {
        super(pos);
        assert (kind != null); // label may be null
        this.kind = kind;
        this.label = label;
    }

    /** Get the kind of the branch. */
    @Override
    public Branch.Kind kind() {
        return this.kind;
    }

    /** Set the kind of the branch. */
    @Override
    public Branch kind(Branch.Kind kind) {
        Branch_c n = (Branch_c) copy();
        n.kind = kind;
        return n;
    }

    /** Get the target label of the branch. */
    @Override
    public Id labelNode() {
        return this.label;
    }

    /** Set the target label of the branch. */
    @Override
    public Branch labelNode(Id label) {
        Branch_c n = (Branch_c) copy();
        n.label = label;
        return n;
    }

    /** Get the target label of the branch. */
    @Override
    public String label() {
        return this.label != null ? this.label.id() : null;
    }

    /** Set the target label of the branch. */
    @Override
    public Branch label(String label) {
        return labelNode(this.label.id(label));
    }

    /** Reconstruct the expression. */
    protected Branch_c reconstruct(Id label) {
        if (label != this.label) {
            Branch_c n = (Branch_c) copy();
            n.label = label;
            return n;
        }

        return this;
    }

    /** Visit the children of the constructor. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Id label = (Id) visitChild(this.label, v);
        return reconstruct(label);
    }

    @Override
    public String toString() {
        return kind.toString() + (label != null ? " " + label.toString() : "");
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(kind.toString());
        if (label != null) {
            w.write(" " + label);
        }
        w.write(";");
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
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
