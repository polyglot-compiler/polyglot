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
  private TypeSystem ts;
  private ErrorQueue eq;
  private LocalContext c;
  private ImportTable it;

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

  public ImportTable getImportTable()
  {
    return it;
  }
}
