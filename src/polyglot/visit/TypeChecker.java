package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Pass;

public class TypeChecker extends NodeVisitor
{
  protected LocalContext c;
  protected ErrorQueue eq;
  protected BitVector errors;
  protected int depth;
  
  public TypeChecker(Pass pass, ExtensionFactory ef,
    TypeSystem ts, ImportTable im, ErrorQueue eq)
  {
    this.eq = eq;

    this.c = ts.getLocalContext(im, ef, pass);
    this.errors = new BitVector();
    this.depth = 0;
  }

  public Node override( Node n)
  {
    if (n.ext instanceof TypeCheckOverride) {
      LocalContext.Mark mark = c.getMark();

      try {
        return ((TypeCheckOverride) n.ext).typeCheck(n, this, c);
      }
      catch ( SemanticException e) {
        eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
                    Annotate.getPosition(n ));
        c.popToMark(mark);
        return n;
      }
    }

    return null;
  }

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
      Position position = e.getPosition();
      if( position == null) {
        position = Annotate.getPosition( n);
      }

      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), position);
      errors.setBit( depth, true);

      n.leaveScope( c);
      return n;
    }
  }
}
