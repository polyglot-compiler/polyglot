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

  public Node override( Node n)
  {
    if( n.hasError()) {
      return n;
    }
    else {
      return null;
    }
  }

  public NodeVisitor enter( Node n)
  {
    n.enterScope( c);
    depth++;
    errors[depth] = false;
    return this;
  }

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    if( errors[ depth]) {
      /* We've seen some error, so propagate back to a statement. */
      if( n instanceof Expression || n instanceof TypeNode) {
        Annotate.setVisitorInfo( n, vinfo);
        return n;
      }
      else
      {
        /* We've hit a statement, so unset the flag and continue. */
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
      errors[depth] = true;
      return n;
    }
  }
}
