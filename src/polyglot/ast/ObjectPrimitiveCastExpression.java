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
    Type rtype = expr.getCheckedType();
    Type ltype = type.getType();

    if( ltype.isPrimitive()) {
      if( rtype.equals(c.getTypeSystem().getObject())) {
      
        if( ltype.equals( c.getTypeSystem().getBoolean())) {
          w.write( "((java.lang.Boolean)(");
        }
        else if( ltype.equals( c.getTypeSystem().getChar())) {
          w.write( "((java.lang.Character)(");
        }
        else if( ltype.equals( c.getTypeSystem().getShort())) {
          w.write( "((java.lang.Short)(");
        }
        else if( ltype.equals( c.getTypeSystem().getInt())) {
          w.write( "((java.lang.Integer)(");
        }
        else if( ltype.equals( c.getTypeSystem().getLong())) {
          w.write( "((java.lang.Long)(");
        }
        else if( ltype.equals( c.getTypeSystem().getFloat())) {
          w.write( "((java.lang.Float)(");
        }
        else if( ltype.equals( c.getTypeSystem().getDouble())) {
          w.write( "((java.lang.Double)(");
        }
        
        expr.translate( c, w);
        
        if( ltype.equals( c.getTypeSystem().getBoolean())) {
          w.write( ")).booleanValue()");
        }
        else if( ltype.equals( c.getTypeSystem().getChar())) {
          w.write( ")).charValue()");
        }
        else if( ltype.equals( c.getTypeSystem().getShort())) {
          w.write( ")).shortValue()");
        }
        else if( ltype.equals( c.getTypeSystem().getInt())) {
          w.write( ")).intValue()");
        }
        else if( ltype.equals( c.getTypeSystem().getLong())) {
          w.write( ")).longValue()");
        }
        else if( ltype.equals( c.getTypeSystem().getFloat())) {
          w.write( ")).floatValue()");
        }
        else if( ltype.equals( c.getTypeSystem().getDouble())) {
          w.write( ")).floatValue()");
        }
      }
      else
        {
          super.translate( c, w);
        }
    }
    else if( ltype.equals( c.getTypeSystem().getObject())) {
      if( rtype.isPrimitive()) {
        
        if( rtype.equals( c.getTypeSystem().getBoolean())) {
          w.write( "(java.lang.Object)new Boolean( ");
        }
        else if( rtype.equals( c.getTypeSystem().getChar())) {
          w.write( "(java.lang.Object)new Character( ");
        }
        else if( rtype.equals( c.getTypeSystem().getShort())) {
          w.write( "(java.lang.Object)new Short( ");
        }
        else if( rtype.equals( c.getTypeSystem().getInt())) {
          w.write( "(java.lang.Object)new Integer( ");
        }
        else if( rtype.equals( c.getTypeSystem().getLong())) {
          w.write( "(java.lang.Object)new Long( ");
        }
        else if( rtype.equals( c.getTypeSystem().getFloat())) {
          w.write( "(java.lang.Object)new Float( ");
        }
        else if( rtype.equals( c.getTypeSystem().getDouble())) {
          w.write( "(java.lang.Object)new Double( ");
        }
        
        expr.translate( c, w);
        
        w.write( ")");
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
}
