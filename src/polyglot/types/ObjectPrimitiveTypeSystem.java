
package jltools.types;

public class ObjectPrimitiveTypeSystem extends StandardTypeSystem
{
  public ObjectPrimitiveTypeSystem( ClassResolver resolver) 
  {
    super ( resolver);
  }

  public boolean isCastValid(Type fromType, Type toType)
    throws TypeCheckException
  {
    if ( (fromType.equals( getObject() ) && toType.isPrimitive() ||
          (toType.equals( getObject() ) && fromType.isPrimitive()))) {
      return true;
    }
    return super.isCastValid(fromType, toType);
  }

  public boolean isAssignableSubtype( Type childType, Type ancestorType)
    throws TypeCheckException
  {
    if( childType.isPrimitive() && ancestorType.equals( getObject())) {
      return true;
    }

    return super.isAssignableSubtype( childType, ancestorType);
  }

  public boolean isImplicitCastValid(Type fromType, Type toType)
    throws TypeCheckException
  {
    if( fromType.isPrimitive() && toType.equals( getObject())) {
      return true;
    }

    return super.isAssignableSubtype( fromType, toType);
  }
}
