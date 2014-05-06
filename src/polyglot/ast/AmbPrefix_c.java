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

import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An {@code AmbPrefix} is an ambiguous AST node composed of dot-separated
 * list of identifiers that must resolve to a prefix.
 */
public class AmbPrefix_c extends Node_c implements AmbPrefix {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Prefix prefix;
    protected Id name;

//    @Deprecated
    public AmbPrefix_c(Position pos, Prefix prefix, Id name) {
        this(pos, prefix, name, null);
    }

    public AmbPrefix_c(Position pos, Prefix prefix, Id name, Ext ext) {
        super(pos, ext);
        assert (name != null); // prefix may be null
        this.prefix = prefix;
        this.name = name;
    }

    @Override
    public Id nameNode() {
        return this.name;
    }

    /** Set the name of the prefix. */
    public AmbPrefix id(Id name) {
        return id(this, name);
    }

    protected <N extends AmbPrefix_c> N id(N n, Id name) {
        if (n.name == name) return n;
        n = copyIfNeeded(n);
        n.name = name;
        return n;
    }

    @Override
    public String name() {
        return this.name.id();
    }

    /** Set the name of the prefix. */
    public AmbPrefix name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public Prefix prefix() {
        return this.prefix;
    }

    /** Set the prefix of the prefix. */
    public AmbPrefix prefix(Prefix prefix) {
        return prefix(this, prefix);
    }

    protected <N extends AmbPrefix_c> N prefix(N n, Prefix prefix) {
        if (n.prefix == prefix) return n;
        n = copyIfNeeded(n);
        n.prefix = prefix;
        return n;
    }

    /** Reconstruct the prefix. */
    protected <N extends AmbPrefix_c> N reconstruct(N n, Prefix prefix, Id name) {
        n = prefix(n, prefix);
        n = id(n, name);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Prefix prefix = visitChild(this.prefix, v);
        Id name = visitChild(this.name, v);
        return reconstruct(this, prefix, name);
    }

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

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot exception check ambiguous node "
                                                + this + ".");
    }

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
