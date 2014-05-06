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

import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * Am immutable representation of a Java statement with a label.  A labeled
 * statement contains the statement being labeled and a string label.
 */
public class Labeled_c extends Stmt_c implements Labeled {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Id label;
    protected Stmt statement;

//    @Deprecated
    public Labeled_c(Position pos, Id label, Stmt statement) {
        this(pos, label, statement, null);
    }

    public Labeled_c(Position pos, Id label, Stmt statement, Ext ext) {
        super(pos, ext);
        assert (label != null && statement != null);
        this.label = label;
        this.statement = statement;
    }

    @Override
    public Id labelNode() {
        return this.label;
    }

    @Override
    public Labeled labelNode(Id label) {
        return labelNode(this, label);
    }

    protected <N extends Labeled_c> N labelNode(N n, Id label) {
        if (n.label == label) return n;
        n = copyIfNeeded(n);
        n.label = label;
        return n;
    }

    @Override
    public String label() {
        return this.label.id();
    }

    @Override
    public Labeled label(String label) {
        return labelNode(this.label.id(label));
    }

    @Override
    public Stmt statement() {
        return this.statement;
    }

    @Override
    public Labeled statement(Stmt statement) {
        return statement(this, statement);
    }

    protected <N extends Labeled_c> N statement(N n, Stmt statement) {
        if (n.statement == statement) return n;
        n = copyIfNeeded(n);
        n.statement = statement;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Labeled_c> N reconstruct(N n, Id label, Stmt statement) {
        n = labelNode(n, label);
        n = statement(n, statement);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id label = visitChild(this.label, v);
        Stmt statement = visitChild(this.statement, v);
        return reconstruct(this, label, statement);
    }

    @Override
    public String toString() {
        return label + ": " + statement;
    }

    @Override
    public Context enterChildScope(Node child, Context c) {
        c = c.pushLabel(label.id());
        return super.enterChildScope(child, c);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Check if the label is multiply defined.
        // See JLS 2nd Ed. | 14.14.
        Context c = tc.context();

        String outerLabel = c.findLabelSilent(label.id());

        if (outerLabel != null && outerLabel.equals(label.id())) {
            throw new SemanticException("Label \"" + label
                    + "\" already in use.");
        }

        return super.typeCheck(tc);
    }

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
