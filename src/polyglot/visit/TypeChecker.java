package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;


public class TypeChecker extends NodeVisitor
{
  private LocalContext c;
  private ErrorQueue eq;
  private boolean errorFlag;
  
  public TypeChecker( TypeSystem ts, ImportTable im, ErrorQueue eq)
  {
    this.eq = eq;

    errorFlag = false;
    c = new LocalContext( im, ts);
  }

  public Node visitBefore( Node n)
  {
    return n.adjustScope( c);
  }

  public Node visitAfter( Node n)
  {
    if( errorFlag) {
      /* We've seen some error, so propagate back to a statement. */
      if( n instanceof Expression) {
        return n;
      }
      else
      {
        /* We've hit a statement, so unset the flag and continue. */
        errorFlag = false;
        return n;
      }
    }

    /* No errors seen so far. */
    try
    {
      return n.typeCheck( c);
    }
    catch( TypeCheckException e)
    {
      int iLine = e.getLineNumber();
      iLine = (iLine == e.INVALID_LINE ? Annotate.getLineNumber( n ) : iLine );

      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, 
                  e.getMessage(),
                  iLine);

      errorFlag = true;
      return n;
    }
  }
}
