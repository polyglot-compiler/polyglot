package jltools.ast;

import jltools.types.*;
import jltools.util.*;

/**
 * An <code>AmbiguousExpression</code> represents any ambiguous Java 
 * expression.
 */
public abstract class AmbiguousExpression extends Expression 
{
  public Node typeCheck( LocalContext c) throws SemanticException
  {
    throw new InternalCompilerError( 
                     "Attempt to type check an ambiguous node.");
  }

  public void translate( LocalContext c, CodeWriter w) 
  {
    throw new InternalCompilerError( 
                     "Attempt to translate an ambiguous node.");
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}

