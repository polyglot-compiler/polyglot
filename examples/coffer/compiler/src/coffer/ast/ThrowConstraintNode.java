/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import coffer.types.ThrowConstraint;

/**
 * An AST node for an exception throw declaration annotated with a key set.
 */
public interface ThrowConstraintNode extends Node {
    TypeNode type();

    KeySetNode keys();

    ThrowConstraint constraint();

    ThrowConstraintNode type(TypeNode type);

    ThrowConstraintNode keys(KeySetNode keys);

    ThrowConstraintNode constraint(ThrowConstraint constraint);
}
