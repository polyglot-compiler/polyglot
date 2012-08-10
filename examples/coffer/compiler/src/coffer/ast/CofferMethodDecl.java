/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import java.util.List;

import polyglot.ast.MethodDecl;

/** An immutable representation of the Coffer method declaration.
 * <code>ConstructorDecl</code> is extended with pre- and post-conditions.
 */
public interface CofferMethodDecl extends MethodDecl {
    KeySetNode entryKeys();

    CofferMethodDecl entryKeys(KeySetNode entryKeys);

    KeySetNode returnKeys();

    CofferMethodDecl returnKeys(KeySetNode returnKeys);

    List<ThrowConstraintNode> throwConstraints();

    CofferMethodDecl throwConstraints(List<ThrowConstraintNode> throwConstraints);
}
