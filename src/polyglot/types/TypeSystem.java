/*
 * Type.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.List;

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

  /**
   * This class represents the <Type, methodType> pair of a method lookup.
   **/
  public static class MethodMatch {
    public final Type onClass;
    public final MethodType method;

    public MethodMatch(Type c, MethodType m) { onClass = c; method = m; }    
  }

  /**
   * This class represents the <Type, fieldType> pair of a field lookup.
   **/
  public static class FieldMatch {
    public final Type onClass;
    public final FieldType field;

    public FieldMatch(Type c, FieldType f) { onClass = c; field = f; }
  }

  ////
  // Functions for two-type comparison.
  ////
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff childClass is not ancestorClass, but childClass descends
   * from ancestorClass.
   **/
  public abstract boolean descendsFrom(JavaClass childClass, 
				       JavaClass ancestorClass);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff childType and ancestorType are distinct
   * non-primitive, non-array types, and childType descends from
   * ancestorType.
   **/
  public abstract boolean descendsFrom(Type childType, 
				       Type ancestorType);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff childType and ancestorType are distinct types,
   *  and a variable of type childType may be legally assigned to a
   *  variable of type ancestorType.
   **/
  public abstract boolean isAssignableSubtype(Type childType, 
					      Type ancestorType);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff a cast from fromType to toType is valid; in other
   * words, some non-null members of fromType are also members of toType.
   **/
  public abstract boolean isCastValid(Type fromType, Type toType);

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an implicit cast from fromType to toType is valid;
   * in other wors, every member of fromType is member of toType.
   **/
  public abstract boolean isImplicitCastValid(Type fromType, Type toType);

  /**
   * Requires: all type arguments are canonical.
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
  public abstract Type getCanonicalType(Type type, Context context);
  /**
   * Checks whether <type> is a valid type in the given context,
   * which may be null.  Returns a description of the error, if any.
   **/
  public abstract String checkTypeOk(Type type, Context context);

  ////
  // Various one-type predicates.
  ////
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an object of type <type> may be thrown.
   **/
  public abstract boolean isThrowable(Type type);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an object of type <type> may be thrown by a method
   * without being declared in its 'throws' clause.
   **/
  public abstract boolean isUncheckedException(Type type);  

  ////
  // Functions for type membership.
  ////
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable iterator of all the FieldMatches defined on
   * type (if any).  The iterator is guaranteed to yeild fields
   * defined on subclasses before those defined on superclasses.
   **/
  public abstract Iterator getFieldsForType(Type type);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable iterator of all the MethodMatches defined on
   * type (if any).  The iterator is guaranteed to yeild methods
   * defined on subclasses before those defined on superclasses.
   **/  
  public abstract Iterator getMethodsForType(Type type);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable iterator of all the FieldMatches named 'name' defined
   * on type (if any).  The iterator is guaranteed to yeild methods
   * defined on subclasses before those defined on superclasses.
   **/
  public abstract Iterator getFielsdNamed(Type type, String name);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable of all the MethodMatches named 'name' defined on
   * type (if any).  The iterator is guaranteed to yeild methods
   * defined on subclasses before those defined on superclasses.
   **/  
  public abstract Iterator getMethodsNamed(Type type, String name);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns the supertype of type, or null if type has no supertype.
   **/
  public abstract Type getSuperType(Type type);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable list of all the interface types which type
   * implements.
   **/
  public abstract List getInterfaces(Type type);

  ////
  // Functions for method testing.
  ////
  /**
   * Returns true iff <type1> is the same as <type2>.
   **/
  public abstract boolean isSameType(MethodType type1, MethodType type2);
  /**
   * Returns true iff <type1> has the same arguments as <type2>
   **/
  public abstract boolean hasSameArguments(MethodType type1, MethodType type2);
  /**
   * If an attempt to call a method of type <method> on <type> would
   * be successful, returns the actual MethodMatch for the method that
   * would be called.  Otherwise returns null.
   *
   * This method uses the name, argument types, and access flags of <method>.
   * The access flags are used to select which protections may be accepted.
   *
   * (Guavac gets this wrong.)
   **/
  public abstract MethodMatch getMethod(Type type, MethodType method);
  /**
   * If an attempt to call a method of type <method> on <type> would
   * be successful, and the method would match on the given <type>,
   * returns the actual MethodMatch for the method that would be
   * called.  Otherwise returns null.
   *
   * This method uses the name, argument types, and access flags of <method>.
   * The access flags are used to select which protections may be accepted.
   *
   * (Guavac gets this wrong.)
   **/
  public abstract MethodMatch getMethodInClass(Type type, MethodType method);
  /**
   * As above, except only returns a match if the argument types are identical.
   **/
  public abstract MethodMatch getExactMethod(Type type, MethodType method);
  public abstract MethodMatch getExactMethodInClass(Type type, MethodType method); 

  
  ////
  // Functions for type->class mapping.
  ////
  public abstract JavaClass getClassForType(Type type);

  ////
  // Functions which yield particular types.
  ////
  public abstract Type getVoid();
  public abstract Type getBoolean();
  public abstract Type getChar();
  public abstract Type getByte();
  public abstract Type getShort();
  public abstract Type getInt();
  public abstract Type getLong();
  public abstract Type getFloat();
  public abstract Type getDouble();
  public abstract Type getObject();
  public abstract Type getThrowable();
  /**
   * Returns a type object for a class type whose name is the provided string.
   * This type may not correspond to a valid class.
   **/
  public abstract Type getTypeWithName(String name);

}

