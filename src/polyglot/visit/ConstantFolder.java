
package jltools.visit;

import jltools.ast.*;
import jltools.types.TypeSystem;

public class ConstantFolder extends NodeVisitor
{
    private ExtensionFactory ef;
    public ConstantFolder(ExtensionFactory ef) {
	this.ef = ef;
    }

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    return n.foldConstants(ef);
  }
  
}
