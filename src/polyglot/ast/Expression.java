/*
 * Expression.java
 */

package jltools.ast;

import jltools.types.Type;
import jltools.visit.SymbolReader;
import jltools.util.Annotate;

/**
 * Expression
 *
 * Overview: An Expression represents any Java expression.  All expressions
 *    must be subclasses of Expression.
 **/
public abstract class Expression extends Node {

  public Node readSymbols( SymbolReader sr)
  {
    return this;
  }

  /**
   * Sets the type of this expression to be <type>.  A 'null' value signifies
   * an unresolved type.
   **/
  public void setCheckedType(Type type) {
    Annotate.setCheckedType(this, type);
  }

  /**
   * Gets the type of this expression.  A 'null' value signifies
   * an unresolved type.
   **/
  public Type getCheckedType() {
    return Annotate.getCheckedType(this);
  }    

   /**
   * Sets the expected type of this expression to be <type>.  
   **/
  public void setExpectedType(Type type) {
    Annotate.setExpectedType(this, type);
  }

  /**
   * Gets the type of this expression.  A 'null' value signifies
   * an unresolved type.
   **/
  public Type getExpectedType() {
    return Annotate.getExpectedType(this);
  }  

  public /* abstract */ int getPrecedence() { return Integer.MAX_VALUE; }
}

