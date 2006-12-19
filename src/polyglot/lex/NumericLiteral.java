/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.lex;

import polyglot.util.Position;

/** A token class for numerical literals. */
public abstract class NumericLiteral extends Literal {
    protected Number val;

  public NumericLiteral(Position position, int sym) { super(position, sym); }

  public Number getValue() { return val; }

  public String toString() { return "numeric literal " + val.toString(); }
}
