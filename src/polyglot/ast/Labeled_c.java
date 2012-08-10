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

import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Am immutable representation of a Java statement with a label.  A labeled
 * statement contains the statement being labelled and a string label.
 */
public class Labeled_c extends Stmt_c implements Labeled {
    protected Id label;
    protected Stmt statement;

    public Labeled_c(Position pos, Id label, Stmt statement) {
        super(pos);
        assert (label != null && statement != null);
        this.label = label;
        this.statement = statement;
    }

    /** Get the label of the statement. */
    @Override
    public Id labelNode() {
        return this.label;
    }

    /** Set the label of the statement. */
    @Override
    public Labeled labelNode(Id label) {
        Labeled_c n = (Labeled_c) copy();
        n.label = label;
        return n;
    }

    /** Get the label of the statement. */
    @Override
    public String label() {
        return this.label.id();
    }

    /** Set the label of the statement. */
    @Override
    public Labeled label(String label) {
        return labelNode(this.label.id(label));
    }

    /** Get the sub-statement of the statement. */
    @Override
    public Stmt statement() {
        return this.statement;
    }

    /** Set the sub-statement of the statement. */
    @Override
    public Labeled statement(Stmt statement) {
        Labeled_c n = (Labeled_c) copy();
        n.statement = statement;
        return n;
    }

    /** Reconstruct the statement. */
    protected Labeled_c reconstruct(Id label, Stmt statement) {
        if (label != this.label || statement != this.statement) {
            Labeled_c n = (Labeled_c) copy();
            n.label = label;
            n.statement = statement;
            return n;
        }

        return this;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Id label = (Id) visitChild(this.label, v);
        Node statement = visitChild(this.statement, v);
        return reconstruct(label, (Stmt) statement);
    }

    @Override
    public String toString() {
        return label + ": " + statement;
    }

    /** Write the statement to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(label + ": ");
        print(statement, w, tr);
    }

    @Override
    public Term firstChild() {
        return statement;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.push(this).visitCFG(statement, this, EXIT);
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Labeled(this.position, this.label, this.statement);
    }

}
