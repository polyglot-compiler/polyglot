/*
 * Type.java
 */

package jltools.types;

import java.util.Iterator;

/**
 * TypeSystem
 *
 * Overview:
 *    A TypeSystem represents a universe of types.  It is responsible for
 *    finding classes to correspond to types, determining relations between
 *    types, and so forth.
 *
 **/
public abstract class TypeSystem {

  /**
   * This class represents the context in which a type lookup is
   * proceeding.
   **/
  public static class Context {   
    public JavaClass inClass;
    public MethodType inMethod;
  }

  ////
  // Functions for two-type comparison.
  ////
  /**
   * Returns true iff childClass is not ancestorClass, but childClass descends
   * from ancestorClass.
   **/
  public abstract boolean descendsFrom(JavaClass childClass, 
				       JavaClass ancestorClass);
  /**
   * Requires: all type arguments have been resolved.
   *
   * Returns true iff childType and ancestorType are distinct
   * non-primitive, non-array types, and childType descends from
   * ancestorType.
   **/
  public abstract boolean descendsFrom(Type childType, 
				       Type ancestorType);
  /**
   * Requires: all type arguments have been resolved.
   *
   * Returns true iff childType and ancestorType are distinct types,
   *  and a variable of type childType may be legally assigned to a
   *  variable of type ancestorType.
   **/
  public abstract boolean isAssignableSubtype(Type childType, 
					      Type ancestorType);
  /**
   * Requires: all type arguments have been resolved.
   *
   * Returns true iff a cast from fromType to toType is valid; in other
   * words, some non-null members of fromType are also members of toType.
   **/
  public abstract boolean isCastValid(Type fromType, Type toType);

  /**
   * Requires: all type arguments have been resolved.
   *
   * Returns true iff an implicit cast from fromType to toType is valid;
   * in other wors, every member of fromType is member of toType.
   **/
  public abstract boolean isImplicitCastValid(Type fromType, Type toType);

  /**
   * Requires: all type arguments have been resolved.
   *
   * Returns true iff type1 and type2 are the same type.
   **/
  public abstract boolean isSameType(Type type1, Type type2);

  ////
  // Functions for one-type checking and resolution.
  ////
  
  /**
   * Returns true iff <type> is a canonical (fully qualified) type.
   **/
  public abstract boolean isCanonical(Type type);
  /**
   * Tries to return the canonical (fully qualified) form of <type>
   * in the provided context, which may be null.
   **/
  public abstract Type resolveType(Type type, Context context);
  /**
   * Checks whether <type> is a valid type in the given context,
   * which may be null.
   **/
  public abstract String checkTypeOk(Type type, Context context);

  ////
  // Functions for type membership.
  ////
  public abstract Iterator getFieldsForType(Type type);
  public abstract Iterator getMethodsForType(Type type);
  public abstract FieldType getFieldNamed(Type type, String name);
  public abstract Iterator getMethodsNamed(Type type, String name);
  public abstract Type getSuperType(Type type);
  public abstract List getInterfaces(Type type);

  ////
  // Functions for method testing.
  ////
  public abstract boolean isSameType(MethodType type1, MethodType type2);
  public abstract boolean hasSameArguments(MethodType type1, MethodType type2);
  public abstract boolean getMethod(Type type, MethodType method);
  public abstract boolean getMethodInClass(Type type, MethodType method);
  public abstract boolean getExactMethod(Type type, MethodType method);
  public abstract boolean getExactMethodInClass(Type type, MethodType method); 
  

  ////
  // Functions for type->class mapping.
  ////
  public abstract JavaClass getClassForType(Type type);
}

