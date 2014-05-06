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
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * An {@code AmbReceiver} is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a receiver.
 */
public class AmbReceiver_c extends AmbPrefix_c implements AmbReceiver {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Type type;

//    @Deprecated
    public AmbReceiver_c(Position pos, Prefix prefix, Id name) {
        this(pos, prefix, name, null);
    }

    public AmbReceiver_c(Position pos, Prefix prefix, Id name, Ext ext) {
        super(pos, prefix, name, ext);
    }

    @Override
    public Type type() {
        return this.type;
    }

    public AmbReceiver type(Type type) {
        return type(this, type);
    }

    protected <N extends AmbReceiver_c> N type(N n, Type type) {
        if (n.type == type) return n;
        n = copyIfNeeded(n);
        n.type = type;
        return n;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return type(tb.typeSystem().unknownType(position()));
    }

    /** Disambiguate the receiver. */
    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        Node n = super.disambiguate(ar);

        if (n instanceof Receiver) {
            return n;
        }

        throw new SemanticException("Could not find type, field, or "
                + "local variable \""
                + (prefix == null ? name.toString() : prefix.toString() + "."
                        + name.toString()) + "\".", position());
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Didn't finish disambiguation; just return.
        return this;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.AmbReceiver(this.position, this.prefix, this.name);
    }

}
