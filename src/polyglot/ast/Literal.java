/*
 * Literal.java
 */

package jltools.ast;

/**
 * Literal
 *
 * Overview: An Literal represents any Java Literal.
 **/
public abstract class Literal extends Expression {
  
  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}
