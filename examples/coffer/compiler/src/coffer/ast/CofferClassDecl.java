/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.ClassDecl;

/**
 * A Coffer class declaration.
 * <code>ClassDecl</code> is extended with a possibly-null key name.
 */
public interface CofferClassDecl extends ClassDecl {
    KeyNode key();

    CofferClassDecl key(KeyNode key);
}
