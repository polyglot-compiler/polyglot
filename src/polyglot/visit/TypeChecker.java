package jltools.visit;

import jltools.ast.Node;
import jltools.ast.NodeVisitor;
import jltools.types.LocalContext;


public class TypeChecker extends NodeVisitor
{
  private LocalContext c;
  
  public TypeChecker( LocalContext c)
  {
    this.c = c;
  }

  public Node visitAfter(Node n)
  {
    return n.typeCheck( c);
  }
}
