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
    
    c = new LocalContext( it, ts);
  }

// XXX document me
  public VisitorNode enter(Node n)
  {
    n.enterScope( c);
    return this;
  }

// XXX document me
  public Node leave( Node n)
  {
    try
    {
      n.removeAmbiguities( c);
    }
    catch( TypeCheckException e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                  Annotate.getLineNumber( n));
      n.setHasError( true);
     
    }
    n.leaveScope();
  }

  public ImportTable getImportTable()
  {
    return it;
  }
}
