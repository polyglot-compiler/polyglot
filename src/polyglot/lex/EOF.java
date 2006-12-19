/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.lex;

import java_cup.runtime.Symbol;
import polyglot.util.Position;

/** Token class for end-of-file. */
public class EOF extends Token {
  public EOF(Position position, int sym) { super(position, sym); }
  public String toString() { return "end of file"; }
}
