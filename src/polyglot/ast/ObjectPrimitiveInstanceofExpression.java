
package jltools.ast;

import jltools.types.*;
import jltools.util.*;


public class ObjectPrimitiveInstanceofExpression extends InstanceofExpression
{ 
  public ObjectPrimitiveInstanceofExpression (Expression expr, TypeNode type) {
    super( expr, type);
  }

  public ObjectPrimitiveInstanceofExpression (Expression expr, Type type) {
    super( expr, type);
  }

  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    Type rtype = type.getType(),
      ltype = expr.getCheckedType();

    if( !ltype.isCastValid( rtype)) {
      throw new TypeCheckException(
                 "Left operand of \"instanceof\" must be castable to "
                 + "the right operand.");
    }

    setCheckedType( c.getTypeSystem().getBoolean());
    return this;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    Type rtype = type.getType();

    if( !rtype.isPrimitive()) {
      super.translate( c, w);
    }
    else {
      translateExpression( expr, c, w);

      w.write( " instanceof ");

      if( rtype.equals( c.getTypeSystem().getBoolean())) {
        w.write( ObjectPrimitiveCastExpression.WRAPPER_PACKAGE + ".Boolean");
      }
      else if( rtype.equals( c.getTypeSystem().getChar())) {
        w.write( ObjectPrimitiveCastExpression.WRAPPER_PACKAGE + ".Character");
      }
      else if( rtype.equals( c.getTypeSystem().getShort())) {
        w.write( ObjectPrimitiveCastExpression.WRAPPER_PACKAGE + ".Short");
      }
      else if( rtype.equals( c.getTypeSystem().getInt())) {
        w.write( ObjectPrimitiveCastExpression.WRAPPER_PACKAGE + ".Integer");
      }
      else if( rtype.equals( c.getTypeSystem().getLong())) {
        w.write( ObjectPrimitiveCastExpression.WRAPPER_PACKAGE + ".Long");
      }
      else if( rtype.equals( c.getTypeSystem().getFloat())) {
        w.write( ObjectPrimitiveCastExpression.WRAPPER_PACKAGE + ".Float");
      }
      else if( rtype.equals( c.getTypeSystem().getDouble())) {
        w.write( ObjectPrimitiveCastExpression.WRAPPER_PACKAGE + ".Double");
      }
    }
  }
}
