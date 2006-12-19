/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.lex;

import polyglot.util.Position;

/** A token class for literals. */
public abstract class Literal extends Token
{
  public Literal(Position position, int sym) { super(position, sym); }
}
