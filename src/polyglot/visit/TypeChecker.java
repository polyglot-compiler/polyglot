package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;


public class TypeChecker extends NodeVisitor
{
  private LocalContext c;
  private ErrorQueue eq;
  
  public TypeChecker( LocalContext c, ErrorQueue eq)
  {
    this.c = c;
    this.eq = eq;
  }

  public Node visitBefore( Node n)
  {
    return n.adjustScope( c);
  }

  public Node visitAfter( Node n)
  {
    try
    {
      return n.typeCheck( c);
    }
    catch( TypeCheckException e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, 
                  e.getMessage(),
                  Annotate.getLineNumber( n));
      return n;
    }
  }
}
