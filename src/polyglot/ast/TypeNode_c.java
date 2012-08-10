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

import polyglot.types.Named;
import polyglot.types.Qualifier;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;

/**
 * A <code>TypeNode</code> is the syntactic representation of a 
 * <code>Type</code> within the abstract syntax tree.
 */
public abstract class TypeNode_c extends Term_c implements TypeNode {
    protected Type type;

    public TypeNode_c(Position pos) {
        super(pos);
    }

    @Override
    public boolean isDisambiguated() {
        return super.isDisambiguated() && type != null && type.isCanonical();
    }

    /** Get the type as a qualifier. */
    @Override
    public Qualifier qualifier() {
        return type();
    }

    /** Get the type this node encapsulates. */
    @Override
    public Type type() {
        return this.type;
    }

    /** Set the type this node encapsulates. */
    @Override
    public TypeNode type(Type type) {
        TypeNode_c n = (TypeNode_c) copy();
        n.type = type;
        return n;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        if (type == null) {
            TypeSystem ts = tb.typeSystem();
            return type(ts.unknownType(position()));
        }
        else {
            return this;
        }
    }

    @Override
    public Term firstChild() {
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public String toString() {
        if (type != null) {
            return type.toString();
        }
        else {
            return "<unknown type>";
        }
    }

    @Override
    public abstract void prettyPrint(CodeWriter w, PrettyPrinter tr);

    @Override
    public String name() {
        if (type instanceof Named) {
            return ((Named) type).name();
        }
        return null;
    }
}
