package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;


/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself. 
 */
public class AmbiguityRemover extends NodeVisitor
{
  protected TypeSystem ts;
  protected ErrorQueue eq;
  protected LocalContext c;
  protected ImportTable it;

  public AmbiguityRemover( TypeSystem ts, ImportTable it, ErrorQueue eq)
  {
    this.ts = ts;
    this.it = it;
    this.eq = eq;
    
    c = new LocalContext( it, ts, this);
  }

  public NodeVisitor enter( Node n)
  {
    n.enterScope( c);
    return this;
  }

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    try
    {
      Node m = n.removeAmbiguities( c);

      m.leaveScope( c);
      return m;
    }
    catch( SemanticException e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                  Annotate.getLineNumber( n));
      // FIXME n.setHasError( true);

      n.leaveScope( c);
      return n;
    }
  }

  public Node override( Node n)
  {
    if ( n instanceof VariableDeclarationStatement)
    {
      try
      {
        Node m = ((VariableDeclarationStatement)n).removeAmbiguities(c, this);
        return m;
      }
      catch ( SemanticException e)
      {
        eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                    Annotate.getLineNumber(n ));
        // FIXME: n.setHasError(true);
        return n;
      }
    }
    return null;
  }

  public ImportTable getImportTable()
  {
    return it;
  }
}
