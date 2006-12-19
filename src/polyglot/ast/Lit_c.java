/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import java.util.List;

/**
 * <code>Lit</code> represents any Java literal.
 */
public abstract class Lit_c extends Expr_c implements Lit
{
    public Lit_c(Position pos) {
	super(pos);
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term entry() {
        return this;
    }

    /**
     * Visit this term in evaluation order.
     */
    public List acceptCFG(CFGBuilder v, List succs) {
        return succs;
    }

    public boolean constantValueSet() {
        return true;
    }

    public boolean isConstant() {
	return true;
    }
    
    public abstract Object constantValue();
}
