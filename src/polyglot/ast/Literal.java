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
   
  public Node visitChildren( NodeVisitor v) 
  {
    /* Nothing to do. */
    return this;
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}

