
package jltools.visit;

import jltools.ast.*;
import jltools.types.TypeSystem;

public class ConstantFolder extends NodeVisitor
{
    private TypeSystem ts;
    public ConstantFolder(TypeSystem ts) {
	this.ts = ts;
    }

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    return n.foldConstants(ts);
  }
  
}
