package jltools.visit;

import jltools.ast.*;
import jltools.types.*;


public class AmbiguityRemover extends NodeVisitor
{
  private TypeSystem ts;
  private LocalContext c;
  private ImportTable it;

  public AmbiguityRemover( TypeSystem ts)
  {
    this.ts = ts;
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
    n.removeAmbiguities( c);
    return null;
  }
}
