package jltools.visit;

import jltools.ast.Node;
import jltools.ast.NodeVisitor;
import jltools.util.CodeWriter;

public class DumpAst extends NodeVisitor
{
  private CodeWriter cw;

  public DumpAst( CodeWriter cw)
  {
    this.cw = cw;
  }

  public Node visitBefore( Node n)
  {
    Node m = n.dump( cw);
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
