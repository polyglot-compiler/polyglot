package jltools.ast;

import jltools.types.*;
import jltools.util.*;

public class ObjectPrimitiveCastExpression extends CastExpression
{  
  public ObjectPrimitiveCastExpression( TypeNode type, Expression expr) {
    super( type, expr);
  }

  public ObjectPrimitiveCastExpression( Type type, Expression expr) {
    super( type, expr);
  }

  public void translate ( LocalContext c, CodeWriter w)
  {
    try
    {
      if( type.getType().equals( c.getTypeSystem().getInt())) {
        if( expr.getCheckedType().descendsFrom( 
                                         c.getTypeSystem().getObject()) || 
            expr.getCheckedType().equals(c.getTypeSystem().getObject() )) {
          w.write( " ((java.lang.Integer)(");
          expr.translate( c, w);
          w.write( " )).intValue() ");
        }
        else
        {
          super.translate( c, w);
        }
      }
      else if( type.getType().equals( c.getTypeSystem().getObject())) {
        if( expr.getCheckedType().equals( c.getTypeSystem().getInt())) {
          w.write( " (java.lang.Object)new Integer( ");
          expr.translate( c, w);
          w.write( " ) ");
        }
        else
        {
        super.translate( c, w);
        }
      }
      else {
        super.translate( c, w);
      }
    }
    catch( TypeCheckException e)
    {
      throw new InternalCompilerError( "Unexpected TypeCheckException: "
                                       + e.getMessage());
    }
  }
}
