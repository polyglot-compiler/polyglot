
package jltools.visit;

import jltools.ast.*;

public class ConstantFolder extends NodeVisitor
{

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    return n.foldConstants();
  }
  
}
