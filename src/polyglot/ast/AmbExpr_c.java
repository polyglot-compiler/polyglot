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
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An <code>AmbExpr</code> is an ambiguous AST node composed of a single
 * identifier that must resolve to an expression.
 */
public class AmbExpr_c extends Expr_c implements AmbExpr {
    protected Id name;

    public AmbExpr_c(Position pos, Id name) {
        super(pos);
        assert (name != null);
        this.name = name;
    }

    /** Get the precedence of the field. */
    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    /** Get the name of the expression. */
    @Override
    public Id id() {
        return this.name;
    }

    /** Set the name of the expression. */
    @Override
    public AmbExpr id(Id id) {
        AmbExpr_c n = (AmbExpr_c) copy();
        n.name = id;
        return n;
    }

    /** Get the name of the expression. */
    @Override
    public String name() {
        return this.name.id();
    }

    /** Set the name of the expression. */
    @Override
    public AmbExpr name(String name) {
        return id(this.name.id(name));
    }

    /** Reconstruct the expression. */
    protected AmbExpr_c reconstruct(Id name) {
        if (name != this.name) {
            AmbExpr_c n = (AmbExpr_c) copy();
            n.name = name;
            return n;
        }
        return this;
    }

    /** Visit the children of the constructor. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = (Id) visitChild(this.name, v);
        return reconstruct(name);
    }

    /** Disambiguate the expression. */
    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        Node n =
                ar.nodeFactory()
                  .disamb()
                  .disambiguate(this, ar, position(), null, name);

        if (n instanceof Expr) {
            return n;
        }

        throw new SemanticException("Could not find field or local "
                + "variable \"" + name + "\".", position());
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Didn't finish disambiguation; just return.
        return this;
    }

    /** Check exceptions thrown by the expression. */
    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot exception check ambiguous node "
                                                + this + ".");
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        tr.print(this, name, w);
    }

    @Override
    public String toString() {
        return name.toString() + "{amb}";
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    @Override
    public Term firstChild() {
        return null;
    }

    /**
     * Visit this term in evaluation order.
     */
    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.AmbExpr(this.position, this.name);
    }

}
