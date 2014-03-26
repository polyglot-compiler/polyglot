/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.Expr;
import polyglot.ast.Stmt;

/**
 * This statement revokes the key associated with a tracked expression.
 * The expression cannot be evaluated after this statement executes.
 */
public interface Free extends Stmt {
    Expr expr();

    Free expr(Expr expr);
}
