package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;


public class TypeChecker extends NodeVisitor
{
  private LocalContext c;
  private ErrorQueue eq;
  private BitVector errors;
  private int depth;
  
  public TypeChecker( TypeSystem ts, ImportTable im, ErrorQueue eq)
  {
    this.eq = eq;

    this.c = ts.getLocalContext( im, this);
    this.errors = new BitVector();
    this.depth = 0;
  }

  /* FIXME
  public Node override( Node n)
  {
    if( n.hasError()) {
      return n;
    }
    else {
      return null;
    }
  }
  */

  public NodeVisitor enter( Node n)
  {
    n.enterScope( c);

    errors.setBit( ++depth, false);

    return this;
  }

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    Node m = null;
    depth--;

    if( errors.getBit( depth + 1)) {
      /* We've seen some error in one of the children, so propagate back to a
       * statement. */
      if( n instanceof Expression || n instanceof TypeNode) {
        // FIXME should this include SwitchStatement.*?
        errors.setBit( depth, true);
        
        n.leaveScope( c);
        return n;
      }
      else
      {
        /* We've hit a statement, so just continue. */
        n.leaveScope( c);
        return n;
      }
    }

    /* No errors seen so far. */
    try
    {
      m = n.typeCheck( c);

      m.leaveScope( c);      
      return m;
    }
    catch( SemanticException e)
    {
      int line = e.getLineNumber();
      if( line == SemanticException.INVALID_LINE) {
        line = Annotate.getLineNumber( n);
      }

      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), line);
      errors.setBit( depth, true);

      n.leaveScope( c);
      return n;
    }
  }
}
