package jltools.visit;

import jltools.ast.Node;
import jltools.ast.NodeVisitor;
import jltools.util.CodeWriter;
import jltools.types.SemanticException;


public class DumpAst extends NodeVisitor
{
  private CodeWriter w;

  public DumpAst( CodeWriter w)
  {
    this.w = w;
  }

  /** 
   * Visit each node before traversal of children. Call <code>dump</code> for
   * that node. Then we begin a new <code>CodeWriter</code> block and traverse
   * the children.
   */
  public NodeVisitor enter( Node n)
  {
    try
    {
      n.dump( w);
    }
    catch( SemanticException e)
    {
      System.err.println( "Caught SemanticException during dump: " 
                          + e.getMessage());
      e.printStackTrace( System.err);
    }

    w.beginBlock();
    return this;
  }

  /**
   * This method is called only after normal traversal of the children. Thus
   * we must end the <code>CodeWriter</code> block that was begun in 
   * <code>enter</code>.
   */
  public Node leave( Node old, Node n, NodeVisitor v)
  {
    w.endBlock();
    return n;
  }
}
