package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;

public class ObjectPrimitiveCastRewriter extends NodeVisitor
{
  TypeSystem ts;
  
  public ObjectPrimitiveCastRewriter( TypeSystem ts) 
  {
    this.ts = ts;
  }
  
  public Node visitAfter( Node n)
  {
    if( n instanceof Expression) {
      Type ctype = ((Expression)n).getCheckedType();
      Type etype = ((Expression)n).getExpectedType();

      // FIXME remove this check
      if( ctype == null || etype == null) return n;

      if( etype.equals( ts.getObject()) &&
            ctype.isPrimitive()) {
        Expression e = new ObjectPrimitiveCastExpression( ts.getObject(),
                                                  (Expression)n);
        e.setCheckedType( etype);
        e.setExpectedType( etype);
        return e;
      }
      else {
        return n;
      }
    }
    else {
      return n;
    }
  }
}
