package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;


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

  public Node visitBefore(Node n)
  {
    return n.adjustScope( c);
  }

  public Node visitAfter( Node n)
  {
    try
    {
      return n.removeAmbiguities( c);
    }
    catch( TypeCheckException e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                  Annotate.getLineNumber( n));
      return n;
    }
  }

  public ImportTable getImportTable()
  {
    return it;
  }
}
