
package jltools.types;

public class IntCastableTypeSystem extends StandardTypeSystem
{
  public IntCastableTypeSystem(ClassResolver resolver) 
  {
    super ( resolver);
  }

  public boolean isCastValid(Type fromType, Type toType)
    throws TypeCheckException
  {
    if ( (fromType.equals( getObject() ) && toType.equals (new PrimitiveType(this, PrimitiveType.INT))) ||
         (toType.equals( getObject() ) && fromType.equals (new PrimitiveType(this, PrimitiveType.INT))))
      return true;
    return super.isCastValid(fromType, toType);
  }
}
