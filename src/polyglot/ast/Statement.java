/*
 * Statement.java
 */

package jltools.ast;

import jltools.visit.SymbolReader;
import jltools.util.CodeWriter;
import jltools.types.LocalContext;

/**
 * Statement
 *
 * Overview: A Statement represents any Java statement.  All statements must
 *    be subclasses of Statement.
 **/
public abstract class Statement extends Node {
  
  public Node readSymbols( SymbolReader sr)
  {
    return null;
  }

  public void translate_substmt(LocalContext c, CodeWriter w) {
      w.allowBreak(4, " ");
      translate_block(c, w);
  }
}

