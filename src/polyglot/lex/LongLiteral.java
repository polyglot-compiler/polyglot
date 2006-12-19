/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.lex;

import java_cup.runtime.Symbol;
import polyglot.util.Position;

/** A token class for long literals. */
public class LongLiteral extends NumericLiteral {
  public LongLiteral(Position position, long l, int sym) {
      super(position, sym);
      this.val = new Long(l);
  }
}
