/*
 * Expression.java
 */

package jltools.ast;

import jltools.types.Type;
import jltools.util.Annotate;

/**
 * Expression
 *
 * Overview: An Expression represents any Java expression.  All expressions
 *    must be subclasses of Expression.
 **/
public abstract class Expression extends Node {

  /**
   * Sets the type of this expression to be <type>.  A 'null' value signifies
   * an unresolved type.
   **/
  public void setType(Type type) {
    Annotate.setType(this, type);
  }

  /**
   * Gets the type of this expression.  A 'null' value signifies
   * an unresolved type.
   **/
  public Type getType() {
    return Annotate.getType(this);
  }    
}

