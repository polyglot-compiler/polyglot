/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.lex;

import java_cup.runtime.Symbol;
import polyglot.util.Position;

/** A token class for keywords. */
public class Keyword extends Token {
    protected String keyword;

  public Keyword(Position position, String s, int sym) {
      super(position, sym);
      keyword = s;
  }

  public String toString() {
      return keyword;
  }
}
