/*
 * Statement.java
 */

package jltools.ast;

import jltools.visit.SymbolReader;

/**
 * Statement
 *
 * Overview: A Statement represents any Java statement.  All statements must
 *    be subclasses of Statement.
 **/
public abstract class Statement extends Node {
  
  public Node readSymbols( SymbolReader sr)
  {
    return this;
  }
}

