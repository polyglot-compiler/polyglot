package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;


public class TypeChecker extends NodeVisitor
{
  private LocalContext c;
  private ErrorQueue eq;
  
  public TypeChecker( LocalContext c, ErrorQueue eq)
  {
    this.c = c;
    this.eq = eq;
  }

  public Node visitAfter(Node n)
  {
    return n.typeCheck( c);
  }
}
