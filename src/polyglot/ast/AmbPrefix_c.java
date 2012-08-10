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

import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An <code>AmbPrefix</code> is an ambiguous AST node composed of dot-separated
 * list of identifiers that must resolve to a prefix.
 */
public class AmbPrefix_c extends Node_c implements AmbPrefix {
    protected Prefix prefix;
    protected Id name;

    public AmbPrefix_c(Position pos, Prefix prefix, Id name) {
        super(pos);
        assert (name != null); // prefix may be null
        this.prefix = prefix;
        this.name = name;
    }

    /** Get the name of the prefix. */
    @Override
    public Id nameNode() {
        return this.name;
    }

    /** Set the name of the prefix. */
    public AmbPrefix id(Id name) {
        AmbPrefix_c n = (AmbPrefix_c) copy();
        n.name = name;
        return n;
    }

    /** Get the name of the prefix. */
    @Override
    public String name() {
        return this.name.id();
    }

    /** Set the name of the prefix. */
    public AmbPrefix name(String name) {
        return id(this.name.id(name));
    }

    /** Get the prefix of the prefix. */
    @Override
    public Prefix prefix() {
        return this.prefix;
    }

    /** Set the prefix of the prefix. */
    public AmbPrefix prefix(Prefix prefix) {
        AmbPrefix_c n = (AmbPrefix_c) copy();
        n.prefix = prefix;
        return n;
    }

    /** Reconstruct the prefix. */
    protected AmbPrefix_c reconstruct(Prefix prefix, Id name) {
        if (prefix != this.prefix || name != this.name) {
            AmbPrefix_c n = (AmbPrefix_c) copy();
            n.prefix = prefix;
            n.name = name;
            return n;
        }

        return this;
    }

    /** Visit the children of the prefix. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Prefix prefix = (Prefix) visitChild(this.prefix, v);
        Id name = (Id) visitChild(this.name, v);
        return reconstruct(prefix, name);
    }

    /** Disambiguate the prefix. */
    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (prefix != null && !prefix.isDisambiguated()) {
            return this;
        }

        return ar.nodeFactory()
                 .disamb()
                 .disambiguate(this, ar, position(), prefix, name);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Didn't finish disambiguation; just return.
        return this;
    }

    /** Check exceptions thrown by the prefix. */
    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot exception check ambiguous node "
                                                + this + ".");
    }

    /** Write the prefix to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (prefix != null) {
            print(prefix, w, tr);
            w.write(".");
        }

        tr.print(this, name, w);
    }

    @Override
    public String toString() {
        return (prefix == null ? name.toString() : prefix.toString() + "."
                + name.toString())
                + "{amb}";
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.AmbPrefix(this.position, this.prefix, this.name);
    }
}
