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
   * performs any initizlation necessary that requries resolvers.
   */
  public abstract void initializeTypeSystem( ClassResolver resolver,
                                             ClassCleaner cleaner)
    throws SemanticException;

  public abstract LocalContext getLocalContext( ImportTable it,
	ExtensionFactory ef, NodeVisitor visitor );

  public abstract FieldInstance newFieldInstance( String name, Type type,
	ReferenceType enclosingType, AccessFlags af);
  public abstract LocalInstance newLocalInstance( String name, Type type,
	AccessFlags af);

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
   * be accessed from TypeContext context. 
   */
  public abstract boolean isAccessible(ReferenceType ctTarget, AccessFlags flags, LocalContext context)
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
  public abstract Type checkAndResolveType(Type type, TypeContext context)
    throws SemanticException;
  public abstract Type checkAndResolveType(Type type, Type contextType)
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
  public abstract FieldInstance getField(Type type, String name, LocalContext context)
    throws SemanticException;
 

  /**
   * Returns the supertype of type, or null if type has no supertype.
   **/
  public abstract ReferenceType getSuperType(ReferenceType type)
    throws SemanticException;

  /**
   * Returns an immutable list of all the interface types which type
   * implements.
   **/
  public abstract List getInterfaces(ReferenceType type)
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
  public abstract MethodTypeInstance getMethod(Type type, MethodType method, 
					LocalContext context)
    throws SemanticException;

  /**
   * If a constructor call on <clazz> with arguments <args> would
   *  succeed in <context> returns the actual ConstructorTypeInstance
   *  for the constructor call, otherwise throws a SemanticException.
   */
  public abstract MethodTypeInstance getConstructor(ClassType clazz,
						    List args,
						    LocalContext context)
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
  public abstract Type getCloneable();
  public abstract Type getSerializable();
  public abstract Type getNullPointerException();
  public abstract Type getClassCastException();
  public abstract Type getOutOfBoundsException();
  public abstract Type getArrayStoreException();
  public abstract Type getArithmeticException();

  /**
   * Returns a non-canonical type object for a class type whose name
   * is the provided string.  This type may not correspond to a valid
   * class.
   **/
  public abstract Type getTypeWithName(String name)
    throws SemanticException;

  /**
   * Returns a type identical to <type>, but with <dims> more array
   * dimensions.  If dims is < 0, array dimensions are stripped.
   **/
  public abstract Type extendArrayDims(Type type, int dims)
    throws SemanticException;

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

  public abstract TypeContext getEmptyContext(ClassResolver resolver);
  public abstract TypeContext getClassContext(ClassType clazz) throws SemanticException;
  public abstract TypeContext getPackageContext(ClassResolver resolver, PackageType type) throws SemanticException;
  public abstract TypeContext getPackageContext(ClassResolver resolver, String name) throws SemanticException;

  /**
   * return the set of objects that should be serialized into the
   * type information for the given ClassType. 
   * Usually only the clazz itself should get encoded, and references
   * to other classes should just have their name written out.
   * If it makes sense for additional types to be fully encoded,
   * (ie, they're necessary to correctly reconstruct the given clazz,
   * and the usual class resolvers can't otherwise find them) they
   * should be returned in the set in addition to clazz.
   */
  public abstract java.util.Set getTypeEncoderRootSet(Type clazz);

  public abstract String translateArrayType(LocalContext c, ArrayType array);
  public abstract String translateClassType(LocalContext c, ClassType clazz);
  public abstract String translatePrimitiveType(LocalContext c, PrimitiveType prim);
}
