package jltools.visit;

import jltools.ast.Node;
import jltools.ast.NodeVisitor;
import jltools.util.CodeWriter;
import jltools.types.TypeCheckException;

public class DumpAst extends NodeVisitor
{
  private CodeWriter cw;

  public DumpAst( CodeWriter cw)
  {
    this.cw = cw;
  }

  public Node visitBefore( Node n)
  {
    Node m = null;

    try
    {
      m = n.dump( cw);
    }
    catch( TypeCheckException e)
    {
      System.err.println( "Caught TypeCheckException during dump: " 
                          + e.getMessage());
      e.printStackTrace( System.err);
    }
    
    if( m == null) {
      cw.beginBlock();
      return null;
    }
    else {
      return m;
    }
  }

  public Node visitAfter( Node n)
  {
    cw.endBlock();
    return n;
  }
}
