/*
 * TypeSystem.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.List;
import jltools.ast.*;

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
   *
   * The 'ImportTable' field is required for type lookups (like checkType and
   * getCanonicalType).  
   *
   * The Type field is required for all method and field lookups.
   *
   * The MethodType field may be null.
   **/
  public static class Context {
    public final ClassResolver table;
    public final ClassType inClass;
    public final MethodType inMethod;
    
    public Context(ClassResolver t, ClassType type, MethodType m) 
      { table = t; inClass = type; inMethod = m; }
    public Context(ClassType type, MethodType m) 
      { table = null; inClass = type; inMethod = m; }
  }

  /**
   * performs any initizlation necessary that requries resolvers.
   */
  public abstract void initializeTypeSystem( ClassResolver resolver,
                                             ClassCleaner cleaner)
    throws SemanticException;

  ////
  // Functions for two-type comparison.
  ////
  /**
   * Returns true iff childClass is not ancestorClass, but childClass descends
   * from ancestorClass.
   **/
  public abstract boolean descendsFrom(Type childClass, 
				       Type  ancestorClass) 
    throws SemanticException ;

  /**
   * Returns true iff childType and ancestorType are non-primitive
   * types, and a variable of type childType may be legally assigned
   * to a variable of type ancestorType.
   **/
  public abstract boolean isAssignableSubtype(Type childType, 
					      Type ancestorType)
    throws SemanticException ;
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff a cast from fromType to toType is valid; in other
   * words, some non-null members of fromType are also members of toType.
   **/
  public abstract boolean isCastValid(Type fromType, Type toType)
    throws SemanticException ;

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an implicit cast from fromType to toType is valid;
   * in other words, every member of fromType is member of toType.
   **/
  public abstract boolean isImplicitCastValid(Type fromType, Type toType)
    throws SemanticException ;

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff type1 and type2 are the same type.
   **/
  public abstract boolean isSameType(Type type1, Type type2);

  /**
   * Returns true if <code>value</code> can be implicitly cast to Primitive type
   * <code>t</code>.
   */
  public abstract boolean numericConversionValid ( Type t, long value);

  /**
   * Requires: all type arguments are canonical.
   * Returns the least common ancestor of Type1 and Type2
   **/
  public abstract Type leastCommonAncestor( Type type1, Type type2)
    throws SemanticException;

  ////
  // Functions for one-type checking and resolution.
  ////
  
  /**
   * Returns true iff <type> is a canonical (fully qualified) type.
   **/
  public abstract boolean isCanonical(Type type)
    throws SemanticException ;

  /**
   * Checks whether a method or field within ctTarget with access flags 'flags' can
   * be accessed from Context context. 
   */
  public abstract boolean isAccessible(ClassType ctTarget, AccessFlags flags, Context context)
    throws SemanticException ;

  /**
   * Returns whether inner is enclosed within outer
   */
  public abstract boolean isEnclosed( ClassType tInner, ClassType tOuter);

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  Otherwise, returns a String
   * describing the error.
   **/
  public abstract Type checkAndResolveType(Type type, Context context)
    throws SemanticException;

  ////
  // Various one-type predicates.
  ////
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an object of type <type> may be thrown.
   **/
  public abstract boolean isThrowable(Type type)
    throws SemanticException;
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an object of type <type> may be thrown by a method
   * without being declared in its 'throws' clause.
   **/
  public abstract boolean isUncheckedException(Type type)
    throws SemanticException;

  ////
  // Functions for type membership.
  ////
  /**
   * Returns the fieldMatch named 'name' defined on 'type' visible in
   * context.  If no such field may be found, returns a fieldmatch
   * with an error explaining why. Considers accessflags
   **/
  public abstract FieldInstance getField(Type type, String name, Context context)
    throws SemanticException;
 

  /**
   * Returns the supertype of type, or null if type has no supertype.
   **/
  public abstract ClassType getSuperType(ClassType type)
    throws SemanticException;

  /**
   * Returns an immutable list of all the interface types which type
   * implements.
   **/
  public abstract List getInterfaces(ClassType type)
    throws SemanticException;

  ////
  // Functions for method testing.
  ////
  /**
   * Returns true iff <type1> is the same as <type2>.
   **/
  public abstract boolean isSameType(MethodType type1, MethodType type2)
    throws SemanticException;

  /**
   * Returns true iff <type1> has the same arguments as <type2>
   **/
  public abstract boolean hasSameArguments(MethodType type1, MethodType type2);

  /**
   * If an attempt to call a method of type <method> on <type> would
   * be successful, returns the actual MethodMatch for the method that
   * would be called.  Otherwise returns a MethodMatch with an error string
   * explaining why no method could be found.
   *
   * If <context> is non-null, only those methods visible in context are
   * considered.
   *
   * Iff <isThis> is true, methods are considered which would only be valid
   * if the target object were equal to the "this" object.
   *
   * This method uses the name, argument types, and access flags of <method>.
   * The access flags are used to select which protections may be accepted.
   *
   * (Guavac gets this wrong.)
   **/
  public abstract MethodTypeInstance getMethod(ClassType type, MethodType method, 
					Context context)
    throws SemanticException;

  ////
  // Functions which yield particular types.
  ////
  public abstract Type getNull();
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
  public abstract Type getString();
  public abstract Type getClass_();
  public abstract Type getThrowable();
  public abstract Type getError();
  public abstract Type getException();
  public abstract Type getRTException();
  /**
   * Returns a non-canonical type object for a class type whose name
   * is the provided string.  This type may not correspond to a valid
   * class.
   **/
  public abstract AmbiguousType getTypeWithName(String name)
    throws SemanticException;

  /**
   * Returns a type identical to <type>, but with <dims> more array
   * dimensions.  If dims is < 0, array dimensions are stripped.
   **/
  public abstract Type extendArrayDims(Type type, int dims)
    throws SemanticException;


    public Node getNewTypeNodeExtension() {
	return null;
    }
    public Node getNewLocalVariableExpressionExtension() {
	return null;
    }
    public Node getNewFieldExpressionExtension() {
	return null;
    }
    public Node getNewLiteralExtension() {
	return null;
    }


  /**
   * Returns a canonical type corresponding to the Java Class object
   * class.  Does not require that <theClass> have a JavaClass
   * theClass.  Does not require that <theClass> have a ClassType
   * registered in this typeSystem.  Does not register the type in
   * this TypeSystem.  
   * this TypeSystem.  For use only by ClassType implementations.
   **/
  public abstract Type typeForClass(Class clazz)
    throws SemanticException;

  /**
   * Given the name for a class, returns the portion which appears to
   * constitute the package -- i.e., all characters up to but not including
   * the last dot, or no characters if the name has no dot.
   **/
  public static String getPackageComponent(String fullName) {
    int lastDot = fullName.lastIndexOf('.');
    return lastDot >= 0 ? fullName.substring(0,lastDot) : "";
  }
 
  /**
   * Given the name for a class, returns the portion which appears to
   * constitute the package -- i.e., all characters after the last
   * dot, or all the characters if the name has no dot.
   **/
  public static String getShortNameComponent(String fullName) {
    int lastDot = fullName.lastIndexOf('.');
    return lastDot >= 0 ? fullName.substring(lastDot+1) : fullName;
  }

  /**
   * Returns true iff the provided class name does not appear to be
   * qualified (i.e., it has no dot.)
   **/
  public static boolean isNameShort(String name) {
    return name.indexOf('.') < 0;
  }

  public static String getFirstComponent(String fullName) {
    int firstDot = fullName.indexOf('.');
    return firstDot >= 0 ? fullName.substring(0,firstDot) : fullName;
  }

  public static String removeFirstComponent(String fullName) {
    int firstDot = fullName.indexOf('.');
    return firstDot >= 0 ? fullName.substring(firstDot+1) : "";
  }
}

