/*
 * AmbiguousExpression.java
 */

package jltools.ast;

/**
 * AmbiguousExpression
 *
 * Overview: An AmbiguousExpression represents any ambiguous Java expression,
 *    such as "a.b.c".
 **/
public abstract class AmbiguousExpression extends Expression {

  public int getPrecedence()
  {
    return 3; // PRECEDENCE_OTHER;
  }
}

