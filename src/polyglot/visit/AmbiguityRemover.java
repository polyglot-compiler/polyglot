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

  public AmbiguityRemover( TypeSystem ts, ErrorQueue eq)
  {
    this.ts = ts;
    this.eq = eq;
  }

  public Node visitBefore(Node n)
  {
    if( n instanceof SourceFileNode)
    {
      SourceFileNode sfn = (SourceFileNode)n;
      it = sfn.getImportTable();
    }
    else if( n instanceof ClassNode)
    {
      ClassNode cn = (ClassNode)n;
      c = new LocalContext( it, new ClassType( ts, cn.getName(), true), 
                            null, ts);
    }
    try
    {
      n.removeAmbiguities( c);
    }
    catch( TypeCheckError e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                  Annotate.getLineNumber( n));
    }
      
    return null;
  }
}
