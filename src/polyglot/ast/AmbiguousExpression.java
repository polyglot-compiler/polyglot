/*
 * AmbiguousExpression.java
 */

package jltools.ast;

import jltools.types.*;
import jltools.util.*;

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

  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    throw new InternalCompilerError( 
                     "Attempt to type check an ambiguous node.");
  }

  public void translate( LocalContext c, CodeWriter w) {}
}

