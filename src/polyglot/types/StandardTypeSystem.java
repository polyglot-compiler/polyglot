/*
 * StandardTypeSystem.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import jltools.ast.NodeVisitor;
import jltools.ast.ExtensionFactory;
import jltools.util.InternalCompilerError;
import jltools.util.Annotate;
import jltools.util.Position;
import jltools.frontend.Pass;

/**
 * StandardTypeSystem
 *
 * Overview:
 *    A StandardTypeSystem is a universe of types, including all Java types.
 **/
public class StandardTypeSystem extends TypeSystem {

  // resolver should handle caching.
  public StandardTypeSystem() {}

  /**
   * Initializes the type system and its internal constants (which depend on
   * the resolver).
   */
  public void initializeTypeSystem( ClassResolver resolver)
    throws SemanticException
  {
    this.resolver = resolver;
    this.emptyResolver = new CompoundClassResolver();

    OBJECT_ = resolver.findClass( "java.lang.Object");
    STRING_ = resolver.findClass( "java.lang.String");
    CLASS_ = resolver.findClass( "java.lang.Class");
    THROWABLE_ = resolver.findClass( "java.lang.Throwable");
    EXCEPTION_ = resolver.findClass( "java.lang.Exception");
    ERROR_ = resolver.findClass( "java.lang.Error");
    RTEXCEPTION_ = resolver.findClass( "java.lang.RuntimeException");
    CLONEABLE_ = resolver.findClass( "java.lang.Cloneable");
    SERIALIZABLE_ = resolver.findClass( "java.io.Serializable");
    NULLPOINTER_EXN_ = resolver.findClass("java.lang.NullPointerException");
    CLASSCAST_EXN_ = resolver.findClass("java.lang.ClassCastException");
    OUTOFBOUNDS_EXN_ = resolver.findClass("java.lang.ArrayIndexOutOfBoundsException");
    ARRAYSTORE_EXN_  = resolver.findClass("java.lang.ArrayStoreException");
    ARITHMETIC_EXN_  = resolver.findClass("java.lang.ArithmeticException");
  }

  public String getWrapperTypeString(PrimitiveType t) {
    switch (t.getKind()) {
      case PrimitiveType.BOOLEAN:
        return "java.lang.Boolean";
      case PrimitiveType.CHAR:
        return "java.lang.Character";
      case PrimitiveType.BYTE:
        return "java.lang.Byte";
      case PrimitiveType.SHORT:
        return "java.lang.Short";
      case PrimitiveType.INT:
        return "java.lang.Integer";
      case PrimitiveType.LONG:
        return "java.lang.Long";
      case PrimitiveType.FLOAT:
        return "java.lang.Float";
      case PrimitiveType.DOUBLE:
        return "java.lang.Double";
      default: 
        return "???";
    }
  }

  public LocalContext getLocalContext( ImportTable it, ExtensionFactory ef,
	Pass pass )
  {
    return new LocalContext( it, this, ef, pass );
  }

  public FieldInstance newFieldInstance( String name, Type type,
	ReferenceType enclosingType, AccessFlags af)
  {
      return new FieldInstance(name, type, enclosingType, af);
  }
	
  public LocalInstance newLocalInstance( String name, Type type,
	AccessFlags af)
  {
      return new LocalInstance(name, type, af);
  }

  ////
  // Functions for two-type comparison.
  ////

  /**
   * Returns true iff childType and ancestorType are distinct
   * reference types, and childType descends from ancestorType.
   **/
  public boolean descendsFrom(Type childType, 
                              Type ancestorType) 
    throws SemanticException 
  {
    if ( childType instanceof AmbiguousType ||
         ancestorType instanceof AmbiguousType)
      throw new InternalCompilerError("Expected fully qualified classes.");

    if(ancestorType instanceof ReferenceType &&
       childType.equals( NULL_)) {
      return true;
    }

    if (ancestorType.equals(childType) ||
        ! (childType instanceof ReferenceType) ||
        ! (ancestorType instanceof ReferenceType) )
    {
      return false;
    }

    boolean isClass = (childType instanceof ClassType) &&
	! ((ClassType)childType).getAccessFlags().isInterface();

    // If the child isn't an interface or array, check whether its
    // supertype is or descends from the ancestorType
    if (isClass) {
      if ( childType.equals ( OBJECT_)) {
        return false;
      }

      ReferenceType parentType = (ReferenceType)
				((ReferenceType)childType).getSuperType();
      if (parentType.equals(ancestorType) ||
	  descendsFrom(parentType, ancestorType)) {
	return true;
      }
    }
    else {
      // if it _is_ an interface or array, check whether the ancestor is Object.
      if (ancestorType.equals(OBJECT_)) {
	return true;
      }
    }

    // Next check interfaces.
    for(Iterator it = getInterfaces((ReferenceType)childType).iterator(); it.hasNext(); ) {
      Type parentType = (Type) it.next();
      if (parentType.equals(ancestorType) ||
	  descendsFrom(parentType, ancestorType))
	return true;            
    }
    return false;
  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff childType and ancestorType are non-primitive
   * types, and a variable of type childType may be legally assigned
   * to a variable of type ancestorType.
   **/
  public boolean isAssignableSubtype(Type childType, 
				     Type ancestorType)
    throws SemanticException{

    if ( childType instanceof AmbiguousType ||
         ancestorType instanceof AmbiguousType) {
      throw new InternalCompilerError("Expected fully qualified classes.");
    } 

    // childType is primitive.
    if (childType instanceof PrimitiveType &&
	ancestorType instanceof PrimitiveType) {
      
      if( ((PrimitiveType)childType).isVoid()) {
        return false;
      }

      PrimitiveType c = (PrimitiveType)childType,
        a = (PrimitiveType)ancestorType;

      if( c.isBoolean() && a.isBoolean()) {
        return true;
      }
      if( c.isBoolean() || a.isBoolean()) {
        return false;
      }

      if( c.getKind() <= a.getKind()) {
        return true;
      }
      else {
        return false;
      }
    }

    if( childType instanceof PrimitiveType ||
        ancestorType instanceof PrimitiveType) {
      return false;
    }

    // childType is array.
    if (childType instanceof ArrayType) {
      ArrayType  child = (ArrayType) childType;
      if (ancestorType instanceof ArrayType) {
	ArrayType ancestor = (ArrayType) ancestorType;
	// Both types are arrays, of the same dimensionality.	
	Type childbase = child.getBaseType();
	Type ancestorbase = ancestor.getBaseType();
	return isAssignableSubtype(childbase, ancestorbase);
      } else {
	// childType is an array, but ancestorType not an array.
	return descendsFrom(childType, ancestorType);    
      }
    } 
    
    // childType is null.
    if (childType instanceof NullType) 
      return true;
 
    // kliger - can we say ReferenceType here?
    // So childType is definitely a ClassType.
    if (! (ancestorType instanceof ReferenceType))
	return false;
    
    return (isSameType(childType, ancestorType) || 
	    descendsFrom(childType, ancestorType));    
  }
  /**
   * Requires: all type arguments are canonical.  ToType is not a NullType.
   *
   * Returns true iff a cast from fromType to toType is valid; in other
   * words, some non-null members of fromType are also members of toType.
   **/
  public boolean isCastValid(Type fromType, Type toType)
    throws SemanticException
  {
    // Are they distinct?
    if (fromType.equals(toType)) return true;
    
    // Are they primitive?
    if (fromType instanceof PrimitiveType) {
      if (! (toType instanceof PrimitiveType)) 
	return false;

      // Distinct primitive types are only convertable if type are numeric.
      if (((PrimitiveType)fromType).isNumeric() && ((PrimitiveType)toType).isNumeric()) 
        return true;
      return false;
    }
    if (toType instanceof PrimitiveType) return false;
  
    if (fromType instanceof NullType) return true;

    // Array cases.
    if (fromType instanceof ArrayType &&
          toType instanceof ArrayType) {
      // FIXME: Make this iterative.
      Type fromBase = ((ArrayType)fromType).getBaseType();
      Type toBase   = ((ArrayType)toType).getBaseType();
      if (fromBase instanceof PrimitiveType) {
        return toBase.equals(fromBase);
      } else if (toBase instanceof PrimitiveType) {
        return false;
      }	
      // Both bases are reference types.
      return isCastValid(fromBase, toBase);
    }
    else if (fromType instanceof ArrayType)
    {
      return descendsFrom(fromType, toType);    
    }
    else if (toType instanceof ArrayType)
    {
      return descendsFrom(toType, fromType);    
    }

    if( fromType instanceof NullType) {
      return (toType instanceof ClassType);
    }

    if (! (fromType instanceof ClassType))
      return false;
    if (! (toType instanceof ClassType))
      return false;

    // From and to are neither primitive nor an array. They are distinct.
    boolean fromInterface = ((ClassType)fromType).getAccessFlags().isInterface();
    boolean toInterface   =   ((ClassType)toType).getAccessFlags().isInterface();
    boolean fromFinal     = ((ClassType)fromType).getAccessFlags().isFinal();
    boolean toFinal       =   ((ClassType)toType).getAccessFlags().isFinal();

    // This is taken from Section 5.5 of the JLS.
    if (!fromInterface) {
      // From is not an interface.
      if (!toInterface) {
	// Nether from nor to is an interface.
	return descendsFrom(fromType, toType) || 
	  descendsFrom(toType, fromType);
      } else if (fromFinal) {
	// From is a final class, and to is an interface
	return descendsFrom(fromType, toType);
      } else {
	// From is a non-final class, and to is an interface.
	return true;
      }
    } else {
      // From is an interface
      if (!toInterface && !toFinal) {
	// To is a non-final class.
	return true;
      } else if (toFinal) {
	// To is a final class.
	return descendsFrom(toType, fromType);
      } else {
	// To and From are both interfaces.
	return true;
      }
    }
  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an implicit cast from fromType to toType is valid;
   * in other words, every member of fromType is member of toType.
   **/
  public boolean isImplicitCastValid(Type fromType, Type toType)
    throws SemanticException
  {
    // TODO: read JLS 5.1 to be sure this is valid.

    if (fromType.equals(toType)) return true;
    
    ////
    // Types are distinct.
    ////

    if (fromType.isPrimitive()) {
      if (! (toType.isPrimitive())) return false;
      ////
      // Both types are primitive...
      ////
      PrimitiveType ptFromType = fromType.toPrimitiveType();
      PrimitiveType ptToType = toType.toPrimitiveType();
      
      if (! ptFromType.isNumeric() || ! ptToType.isNumeric())       
	return false;

      // ...and numeric.
      switch (ptFromType.getKind()) 
	{
	case PrimitiveType.VOID:
          return false;
	case PrimitiveType.BYTE:
	case PrimitiveType.SHORT:
	case PrimitiveType.CHAR:
	  if (ptToType.getKind() == PrimitiveType.INT) return true;
	case PrimitiveType.INT:
	  if (ptToType.getKind() == PrimitiveType.LONG) return true;
	case PrimitiveType.LONG:
	  if (ptToType.getKind() == PrimitiveType.FLOAT) return true;
	case PrimitiveType.FLOAT:
	  if (ptToType.getKind() == PrimitiveType.DOUBLE) return true;
	case PrimitiveType.DOUBLE:
	default:
	  ;
	}
      return false;
    }
    if (toType instanceof PrimitiveType) return false;
    
    return isAssignableSubtype(fromType,toType);
  }

  /**
   * Returns true iff type1 and type2 are the same type.
   **/
  public boolean isSameType(Type type1, Type type2)
  {
    return type1.equals(type2);
  }

  /**
   * Returns true if <code>value</code> can be implicitly cast to Primitive type
   * <code>t</code>.
   */
  public  boolean numericConversionValid ( Type t, long value)
  {
    if ( !(t instanceof PrimitiveType)) return false;
    
    int kind = ((PrimitiveType)t).getKind();
    switch (kind)
    {
    case PrimitiveType.BYTE: return (Math.abs( value) <= Byte.MAX_VALUE);
    case PrimitiveType.SHORT: return (Math.abs( value) <= Short.MAX_VALUE);
    case PrimitiveType.CHAR: return (value >= 0 && value <= Character.MAX_VALUE);
    case PrimitiveType.INT: return ( Math.abs(value) <= Integer.MAX_VALUE);
    case PrimitiveType.LONG: return true;
    default: return false;
    }
  }


  ////
  // Functions for one-type checking and resolution.
  ////
  
  /**
   * Returns true iff <type> is a canonical (fully qualified) type.
   **/
  public boolean isCanonical(Type type) throws SemanticException {
    return type.isCanonical();
  }
      
  /**
   * Checks whether a method, field or inner class within ctTarget with access flags 'flags' can
   * be accessed from Context context, where context is a class type.
   */
  public boolean isAccessible(ReferenceType rtTarget, AccessFlags flags, LocalContext context) 
    throws SemanticException 
  {

    // check if in same class or public 
    if ( isSameType( rtTarget, context.getCurrentClass() ) ||
         flags.isPublic())
      return true;

    if (! rtTarget.isClassType()) {
      return false;
    }

    ClassType ctTarget = (ClassType) rtTarget;

    // check if context is an inner class of ctEnclosingClass, in which case protection doesnt matter
    if ( isEnclosed ( context.getCurrentClass(), ctTarget))
      return true;
    // check if ctTarget is an inner of context, in which case protection doesnt matter either
    if ( isEnclosed ( ctTarget, context.getCurrentClass()))
      return true;

    if ( ! (context.getCurrentClass() instanceof ClassType))
    {
      throw new InternalCompilerError("Internal error: Context is not a Classtype");
    }

    ClassType ctContext = (ClassType)context.getCurrentClass();
    
    // check for package level scope. ( same package and flags has package level scope.
    if ( ctTarget.getPackage() == null && ctContext.getPackage() == null && flags.isPackage())
      return true;

    // kliger: this used to only allow access if the context and the
    //   target are in the same package and the flags have package-level
    //   access set.
    // However, JLS2 6.6.1 says that if the protected flag is set, then
    //   if the package is the same for target and context, then access
    //   is allowed, as well.  (in addition to the normal "subclasses get
    //   access" rule for protected members).
    // This is confusing for C++ programmers like me.
    if (ctTarget.getPackage() != null &&
        ctTarget.getPackage().equals (ctContext.getPackage()) &&
        (flags.isPackage() || flags.isProtected()))
      return true;
    
    // protected
    if ( ctContext.descendsFrom( ctTarget ) &&
         flags.isProtected())
      return true;
    
    // else, 
    return false;
  }

  public boolean isEnclosed(ClassType tInner, ClassType tOuter)
  {
    ClassType ct = tInner; 
    while ( (ct = ct.getContainingClass()) != null &&
            ! ct.equals(tOuter));
    return ct != null;
  }

  public void cleanClass(ClassType type) throws SemanticException {
    if (type instanceof ParsedClassType) {
        ImportTable it = ((ParsedClassType) type).importTable();
	cleanSuperTypes(type, getEmptyContext(it));
	cleanClass(type, getClassContext(type, it));
    }
    else {
	cleanSuperTypes(type, getEmptyContext(resolver));
	cleanClass(type, getClassContext(type, resolver));
    }
  }

  public void cleanSuperTypes(ClassType type, TypeContext tc)
      throws SemanticException {
    TypeSystem.report(1, "Cleaning super types of " + type);

    if (! (type instanceof ParsedClassType)) {
        return;
    }

    ParsedClassType ct = (ParsedClassType) type;

    Type superType = ct.getSuperType();

    if (superType != null) {
	ClassType superClazz = (ClassType) checkAndResolveType(superType, tc);
	cleanClass(superClazz);
	ct.setSuperType(superClazz);
    }

    for (ListIterator i = ct.getInterfaces().listIterator(); i.hasNext(); ) {
      Type s = (Type) i.next();
      ClassType cs = (ClassType) checkAndResolveType(s, tc);
      cleanClass(cs);
      i.set(cs);
    }

    if (ct.isAnonymous()) {
      // If the class is anonymous, the parser created the node assuming
      // the super type is an interface, not a class.  After cleaning the
      // super type, if the assumption proves false, correct the mistake.

      if (ct.getSuperType() != null || ct.getInterfaces().size() != 1) {
	throw new InternalCompilerError(ct, "Anonymous classes should be " +
	  "constructed with a null superclass and one super-interface");
      }

      ClassType s = (ClassType) ct.getInterfaces().get(0);

      if (! s.getAccessFlags().isInterface()) {
	ct.setSuperType(s);
	ct.getInterfaces().clear();
      }
      else {
	ct.setSuperType((ClassType) getObject());
      }
    }
    else {
      ClassType s = (ClassType) ct.getSuperType();

      if (s == null) {
	s = (ClassType) getObject();
	ct.setSuperType(s);
      }

      if (s.getAccessFlags().isInterface()) {
	  throw new SemanticException("Class " + ct +
				      " cannot extend interface " + s + ".",
				      Annotate.getPosition(ct));
      }

      for (Iterator i = ct.getInterfaces().iterator(); i.hasNext(); ) {
	ClassType si = (ClassType) i.next();
	if (! si.getAccessFlags().isInterface()) {
	  throw new SemanticException("Class " + ct +
				      " cannot implement class " + s + ".",
				      Annotate.getPosition(ct));
	}
      }
    }
  }

  public void cleanClass(ClassType type, TypeContext tc)
      throws SemanticException {
    TypeSystem.report(1, "Cleaning " + type);

    if (! (type instanceof ParsedClassType)) {
        return;
    }

    ParsedClassType ct = (ParsedClassType) type;

    if (ct.getContainingClass() != null) {
      ct.setContainingClass(
	(ClassType) checkAndResolveType(ct.getContainingClass(), tc));
    }

    for (Iterator i = ct.getMethods().iterator(); i.hasNext(); ) {
      MethodTypeInstance mti = (MethodTypeInstance) i.next();

      try {
	  mti.setReturnType(checkAndResolveType(mti.getReturnType(), tc));

	  ListIterator j;

	  j = mti.argumentTypes().listIterator();

	  while (j.hasNext()) {
	    j.set(checkAndResolveType((Type) j.next(), tc));
	  }

	  j = mti.exceptionTypes().listIterator();

	  while (j.hasNext()) {
	    j.set(checkAndResolveType((Type) j.next(), tc));
	  }
      }
      catch (NoClassException e) {
	  if (e.getPosition() == null) {
	      throw new NoClassException(e.getMessage(),
		      			  Annotate.getPosition(mti));
	  }
	  throw e;
      }
      catch (SemanticException e) {
	  if (e.getPosition() == null) {
	      throw new SemanticException(e.getMessage(),
		      			  Annotate.getPosition(mti));
	  }
	  throw e;
      }
    }

    for (Iterator i = ct.getFields().iterator(); i.hasNext(); ) {
      FieldInstance fi = (FieldInstance) i.next();

      try {
	  fi.setType(checkAndResolveType(fi.getType(), tc));
      }
      catch (NoClassException e) {
	  if (e.getPosition() == null) {
	      throw new NoClassException(e.getMessage(),
		      			  Annotate.getPosition(fi));
	  }
	  throw e;
      }
      catch (SemanticException e) {
	  if (e.getPosition() == null) {
	      throw new SemanticException(e.getMessage(),
		      			  Annotate.getPosition(fi));
	  }
	  throw e;
      }
    }
  }

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  Otherwise, returns a String
   * describing the error.
   **/
  public Type checkAndResolveType(Type type, TypeContext context)
    throws SemanticException {

    Type t = checkAndResolve(type, context);

    if (t.isPackageType()) {
	throw new NoClassException("Type " + type.getTypeString() +
	    " not found.");
    }

    return t;
  }

  public Type checkAndResolve(Type type, TypeContext context)
    throws SemanticException {

    try {
      TypeSystem.report(2, "Checking: " + type);

      if (type.isCanonical()) {
	TypeSystem.report(2, "::Resolved: canonical to " + type);
	return type;
      }

      if (type instanceof ArrayType) {
	ArrayType at = (ArrayType) type;
	Type base = at.getBaseType();
	Type result = checkAndResolveType(base, context);

	Type t = new ArrayType(this, (Type)result);

	TypeSystem.report(2, "::Resolved: " + type + " to " + t);

	return t;
      }
      
      if (! (type instanceof AmbiguousType)) 
	throw new InternalCompilerError(
	  "Found a non-canonical, non-array, non-ambiguous type: " +
	      type.getTypeString() + ".");

      Type t = checkAndResolveAmbiguousType((AmbiguousType) type, context);

      TypeSystem.report(2, "::Resolved: " + type + " to " + t);

      return t;
    }
    catch (SemanticException e) {
      TypeSystem.report(2, "::Exception: " + e.getMessage());
      throw e;
    }
  }

  public Type checkAndResolveType(Type type, Type contextType) throws SemanticException {
    if (contextType.isClassType()) {
	TypeContext classContext = getClassContext((ClassType) contextType);
	return checkAndResolve(type, classContext);
    }
    else if (contextType.isPackageType()) {
	TypeContext packageContext = getPackageContext(resolver,
	    (PackageType) contextType);
	return checkAndResolve(type, packageContext);
    }
    else {
	throw new NoClassException("Type " + type.getTypeString() +
		" not found.");
    }
  }

  protected Type checkAndResolveAmbiguousType(AmbiguousType type,
    TypeContext context) throws SemanticException {

    if (! (type instanceof AmbiguousNameType)) {
      throw new InternalCompilerError(
	"Found a non-canonical, non-array, non-ambiguous-name type.");
    }

    AmbiguousNameType ambType = (AmbiguousNameType) type;

    // If the ambiguous name is just an identifier, look it up in the context.
    if (ambType.isShort()) {
      return context.getType(ambType.getName());
    }

    // If the ambiguous name is qualified: classify the prefix to create
    // a new context in which to lookup the unqualified name.
    Type prefixType = ambType.getPrefix();

    if (prefixType instanceof AmbiguousType) {
	prefixType = checkAndResolveAmbiguousType((AmbiguousType) prefixType,
						context);
    }

    // Lookup the unqualified name in the context of the prefix.
    if (prefixType.isClassType()) {
	TypeContext classContext = getClassContext((ClassType) prefixType);
	return classContext.getType(ambType.getName());
    }
    else if (prefixType.isPackageType()) {
	TypeContext packageContext = getPackageContext(resolver,
						      (PackageType) prefixType);
	return packageContext.getType(ambType.getName());
    }
    else {
	throw new NoClassException("Type " + type + " not found.");
    }
  }

  ////
  // Various one-type predicates.
  ////
  /**
   * Returns true iff an object of type <type> may be thrown.
   **/
  public boolean isThrowable(Type type) throws SemanticException {
    return descendsFrom(type,THROWABLE_) || type.equals(THROWABLE_);
  }
  /**
   * Returns true iff an object of type <type> may be thrown by a method
   * without being declared in its 'throws' clause.
   **/
  public boolean isUncheckedException(Type type) throws SemanticException {
    return descendsFrom(type,ERROR_) || type.equals(ERROR_) || 
      descendsFrom(type,RTEXCEPTION_) || type.equals(RTEXCEPTION_);
  }

  ////
  // Functions for type membership.
  ////
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns the fieldMatch named 'name' defined on 'type' visible in
   * context.  If no such field may be found, returns a fieldmatch
   * with an error explaining why. name and context may be null, in which case
   * they will not restrict the output.
   **/
  public FieldInstance getField(Type t, String name, LocalContext context ) 
    throws SemanticException
  {
    FieldInstance fi = null, fiEnclosing = null, fiTemp = null;
    ReferenceType type = null;

    if (t == null)
    {
      throw new InternalCompilerError("getField called with null type");
    }

    if ( !( t instanceof ReferenceType))
    {
      throw new SemanticException("Field access valid only on reference types.");
    }
    type = (ReferenceType)t;
    do 
    {
      for (Iterator i = type.getFields().iterator(); i.hasNext() ; )
      {
	fi = (FieldInstance)i.next();
	if ( fi.getName().equals(name))
	{
	  if ( isAccessible( type, fi.getAccessFlags(), context))
	  {
	    return fi;
	  }
	  throw new SemanticException(" Field \"" + name + "\" found in \"" + 
				       type.getTypeString() + 
				       "\", but with wrong access permissions.");
	}
      }
    }
    while ( (type = (ReferenceType)type.getSuperType()) != null);
    throw new SemanticException( "Field \"" + name + "\" not found in context "
				  + t.getTypeString() );
  }

 /**
   * Requires: all type arguments are canonical.
   * 
   * Returns the MethodMatch named 'name' defined on 'type' visibile in
   * context.  If no such field may be found, returns a fieldmatch
   * with an error explaining why. Considers accessflags.
   **/
  public MethodTypeInstance getMethod(Type t, MethodType method, 
                                      LocalContext context)
    throws SemanticException
  {
    if (t == null)
    {
      throw new InternalCompilerError("getMethod called with null type");
    }

    if ( !( t instanceof ReferenceType))
    {
      throw new SemanticException("Method access valid only on reference types.");
    }

    ReferenceType type = (ReferenceType) t;

    List lAcceptable = new java.util.ArrayList();
    getMethodSet ( lAcceptable, type, method, context);
    
    if (lAcceptable.size() == 0)
      throw new SemanticException ( "No valid method call found for \"" + 
                                     method.getName() + "\" in "+ t + ".");

    // now, use JLS 15.11.2.2
    Object [] mtiArray = lAcceptable.toArray(  );
    MostSpecificComparator msc = new MostSpecificComparator();
    java.util.Arrays.sort( mtiArray, msc);

    // now check to make sure that we have a maximal most specific method.
    // (if we did, it would be in the 0th index.
    for ( int i = 1 ; i < mtiArray.length; i++)
    {
      if (msc.compare ( mtiArray[0], mtiArray[i]) > 0)
        throw new SemanticException("Ambiguous method \"" + method.getName() 
                                     + "\". More than one invocations are valid"
                                     + " from this context:"
				     + java.util.Arrays.asList(mtiArray));
    }
    
    // ok. mtiArray[0] is maximal most specific, so return it.
    return (MethodTypeInstance)mtiArray[0];
  }

  /** 
   * Class to handle the comparisons; dispactes to moreSpecific method. 
   * <p> Should really be an anonymous class, but isn't because the jltools
   * compiler doesn't yet handle anonymous classes.
   */
  protected class MostSpecificComparator implements java.util.Comparator
  {

    public MostSpecificComparator() {}

    public int compare ( Object o1, Object o2)
    {
      if ( !( o1 instanceof MethodTypeInstance ) ||
           !( o2 instanceof MethodTypeInstance ))
        throw new ClassCastException();
      MethodTypeInstance mti1 = (MethodTypeInstance)o1;
      MethodTypeInstance mti2 = (MethodTypeInstance)o2;

      if (moreSpecific (mti1, mti2))
	return -1;

      if (moreSpecific (mti2, mti1))
	return 1;

      // otherwise equally maximally specific

      // JLS2 15.12.2.2 "two or more maximally specific methods"
      // if both abstract or not abstract, equally applicable
      // otherwise the non-abstract is more applicable
      if (mti1.getAccessFlags().isAbstract() ==
	  mti2.getAccessFlags().isAbstract())
	return 0;
      else if (mti1.getAccessFlags().isAbstract())
	return 1;
      else
	return -1;
    }
  }


  /**
   * populates the list lAcceptible with those MethodTypeInstances which are 
   * Applicable and Accessible as defined by JLS 15.11.2.1
   */
  private void getMethodSet(List lAcceptable, ReferenceType type, MethodType method, 
                            LocalContext context)
    throws SemanticException
  {
    MethodTypeInstance mti = null;

    if (type == null)
    {
      throw new InternalCompilerError(
	"getMethodSet called with null reference type");
    }

    LinkedList typeQueue = new LinkedList();
    Set visitedTypes = new HashSet();
    typeQueue.addLast(type);

    while (!typeQueue.isEmpty())
    {
      type = (ReferenceType)typeQueue.removeFirst();
      if (visitedTypes.contains(type))
	continue;

      for (Iterator i = type.getMethods().iterator(); i.hasNext() ; )
      {
	mti = (MethodTypeInstance)i.next();
	if ( methodCallValid( mti, method))
	{
	  if ( isAccessible( type, mti.getAccessFlags(), context))
	  {
	    lAcceptable.add (mti);
	  }
	}
      }

      visitedTypes.add(type);
      if (type.getSuperType() != null)
	typeQueue.addLast(type.getSuperType());
      for (Iterator i = type.getInterfaces().iterator(); i.hasNext() ; ) {
	Object iface = i.next();
	if (iface != null)
	  typeQueue.addLast(iface);
      }

    }

  }

  /**
   * Returns whether MethodType 1 is <i>more specific</i> than MethodTypeInstance 2, 
   * where <i>more specific</i> is defined as JLS 15.11.2.2
   * <p>
   * Note: There is a fair amount of guesswork since the JLS does not include any 
   * info regarding java 1.2, so all inner class rules are found empirically
   * using jikes and javac.
   */
  /**
   * Note: java 1.2 rule is described in JLS2 in section 15.12.2.2
   */
  private boolean moreSpecific(MethodTypeInstance mti1, MethodTypeInstance mti2)
  {
    try
    {
      // rule 1:
      ReferenceType t1 = mti1.getEnclosingType();
      ReferenceType t2 = mti2.getEnclosingType();

      if (t1 instanceof ClassType && t2 instanceof ClassType) {
	if ( ! (t1.descendsFrom(t2) || t1.equals(t2) ||
		isEnclosed((ClassType) t1, (ClassType) t2)))
	  return false;
      }
      else {
	if ( ! (t1.descendsFrom(t2) || t1.equals(t2)))
	  return false;
      }

      // rule 2:
      return methodCallValid ( mti2, mti1) ;

    }
    catch (SemanticException tce)
    {
      return false;
    }
  }

  /**
   * Returns the ConstructorTypeInstance correpsonding to the
   *   constructor call for the given class on the given args
   */
  public MethodTypeInstance getConstructor(ClassType clazz, List args,
					   LocalContext context)
    throws SemanticException
  {
    return context.getMethod(clazz,
			     new ConstructorType(context.getTypeSystem(),
						 clazz, args));
  }
					   


  /**
   * Returns the supertype of type, or null if type has no supertype.
   **/
  public ReferenceType getSuperType(ReferenceType type) throws SemanticException
  {
    return (ReferenceType)type.getSuperType();
  }

  /**
   * Returns an immutable list of all the interface types which type
   * implements.
   **/
  public List getInterfaces(ReferenceType type) throws SemanticException
  {
    return type.getInterfaces();
  }

  /**
   * Returns true iff <type1> is the same as <type2>.
   **/
  public boolean isSameType(MethodType type1, MethodType type2) 
  {
    return ( type1.equals(type2));
  }

  /**
   * Requires: all type arguments are canonical.
   * Returns the least common ancestor of Type1 and Type2
   **/
  public Type leastCommonAncestor( Type type1, Type type2) throws SemanticException
  {
    if (( type1 instanceof PrimitiveType ) &&
        ( type2 instanceof PrimitiveType ))
    {
      if( ((PrimitiveType)type1).isBoolean()) {
        if( ((PrimitiveType)type2).isBoolean()) {
          return getBoolean();
        }
        else {
          throw new SemanticException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
        }

      }
      if( ((PrimitiveType)type2).isBoolean()) {
        throw new SemanticException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
      }
      if( ((PrimitiveType)type1).isVoid()) {
        if( ((PrimitiveType)type2).isVoid()) {
          return getVoid();
        }
        else {
          throw new SemanticException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
        }
      }
      if( ((PrimitiveType)type2).isVoid()) {
        throw new SemanticException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
      } 
      
      return new PrimitiveType( this, Math.max ( 
                                        ((PrimitiveType) type1).getKind(), 
                                        ((PrimitiveType) type2).getKind() ));
    }

    if ( ( type1 instanceof ArrayType ) && ( type2 instanceof ArrayType ) ) {
	ArrayType t1 = (ArrayType) type1;
	ArrayType t2 = (ArrayType) type2;

	Type base = leastCommonAncestor(t1.getBaseType(), t2.getBaseType());

	return new ArrayType(this, base);
    }
    
    if ( ( type1 instanceof ReferenceType ) && ( type2 instanceof NullType))
      return type1;
    if ( ( type2 instanceof ReferenceType ) && ( type1 instanceof NullType))
      return type2;
    
    if (!( type1 instanceof ReferenceType) ||
        !( type2 instanceof ReferenceType)) {
      throw new SemanticException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
    }
    
    ReferenceType tSuper = (ReferenceType)type1;
    
    while ( ! ( type2.descendsFrom ( tSuper ) ||
                type2.equals( tSuper )) &&
            tSuper != null) {
      tSuper = (ReferenceType)tSuper.getSuperType();
    }

    if ( tSuper == null) {
      throw new SemanticException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
    }
    return tSuper;
                                           
  }


  ////
  // Functions for method testing.
  ////

  /**
   * Returns true iff <type1> has the same arguments as <type2>
   **/
  public boolean hasSameArguments(MethodType type1, MethodType type2)
  {
    List lArgumentTypes1 = type1.argumentTypes();
    List lArgumentTypes2 = type2.argumentTypes();
    
    if (lArgumentTypes1.size() != lArgumentTypes2.size())
      return false;
    
    Iterator iArgumentTypes1 = lArgumentTypes1.iterator();
    Iterator iArgumentTypes2 = lArgumentTypes2.iterator();
    
    for ( ; iArgumentTypes1.hasNext() ; )
      if ( ! isSameType ( (Type)iArgumentTypes1.next(), (Type)iArgumentTypes2.next() ) )
        return false;
    return true;
  }

  /**
   * Returns whether the arguments for MethodType call can call
   * MethodTypeInstance prototype
   */
  public boolean methodCallValid( MethodTypeInstance prototype, MethodType call) 
    throws SemanticException
  {

    if ( ! prototype.getName().equals(call.getName()))
      return false;

    List lArgumentTypesProto = prototype.argumentTypes();
    List lArgumentTypesCall = call.argumentTypes();
    
    if (lArgumentTypesProto.size() != lArgumentTypesCall.size())
      return false;
    
    Iterator iArgumentTypesProto = lArgumentTypesProto.iterator();
    Iterator iArgumentTypesCall  = lArgumentTypesCall.iterator();
    
    for ( ; iArgumentTypesProto.hasNext() ; )
    {
      if ( !isImplicitCastValid (  (Type)iArgumentTypesCall.next(),  (Type)iArgumentTypesProto.next() ) )
        return false;
    }
    
    return true;
  }

  ////
  // Functions which yield particular types.
  ////
  public Type getNull()    { return NULL_; }
  public Type getVoid()    { return VOID_; }
  public Type getBoolean() { return BOOLEAN_; }
  public Type getChar()    { return CHAR_; }
  public Type getByte()    { return BYTE_; }
  public Type getShort()   { return SHORT_; }
  public Type getInt()    { return INT_; }
  public Type getLong()    { return LONG_; }
  public Type getFloat()   { return FLOAT_; }
  public Type getDouble()  { return DOUBLE_; }
  public Type getObject()  { return OBJECT_; }
  public Type getClass_()   { return CLASS_; }
  public Type getString()   { return STRING_; }
  public Type getThrowable() { return THROWABLE_; }
  public Type getError() { return ERROR_; }
  public Type getException() { return EXCEPTION_; }
  public Type getRTException() { return RTEXCEPTION_; }
  public Type getCloneable() { return CLONEABLE_; }
  public Type getSerializable() { return SERIALIZABLE_; }
  public Type getNullPointerException() { return NULLPOINTER_EXN_; }
  public Type getClassCastException()   { return CLASSCAST_EXN_; }
  public Type getOutOfBoundsException() { return OUTOFBOUNDS_EXN_; }
  public Type getArrayStoreException()  { return ARRAYSTORE_EXN_; }
  public Type getArithmeticException()  { return ARITHMETIC_EXN_; }


  protected final Type NULL_    = new NullType(this);
  protected final Type VOID_    = new PrimitiveType(this, PrimitiveType.VOID);
  protected final Type BOOLEAN_ = new PrimitiveType(this, PrimitiveType.BOOLEAN);
  protected final Type CHAR_    = new PrimitiveType(this, PrimitiveType.CHAR);
  protected final Type BYTE_    = new PrimitiveType(this, PrimitiveType.BYTE);
  protected final Type SHORT_   = new PrimitiveType(this, PrimitiveType.SHORT);
  protected final Type INT_     = new PrimitiveType(this, PrimitiveType.INT);
  protected final Type LONG_    = new PrimitiveType(this, PrimitiveType.LONG);
  protected final Type FLOAT_   = new PrimitiveType(this, PrimitiveType.FLOAT);
  protected final Type DOUBLE_  = new PrimitiveType(this, PrimitiveType.DOUBLE);
  protected Type OBJECT_;
  protected Type CLASS_;
  protected Type STRING_;
  protected Type THROWABLE_;
  protected Type ERROR_;
  protected Type EXCEPTION_;
  protected Type RTEXCEPTION_;
  protected Type CLONEABLE_;
  protected Type SERIALIZABLE_;
  protected Type NULLPOINTER_EXN_;
  protected Type CLASSCAST_EXN_;
  protected Type OUTOFBOUNDS_EXN_;
  protected Type ARRAYSTORE_EXN_;
  protected Type ARITHMETIC_EXN_;
  
  protected ClassResolver resolver; //Should do its own caching.
  protected ClassResolver emptyResolver;

  /**
   * Returns a non-canonical type object for a class type whose name
   * is the provided string.  This type may not correspond to a valid
   * class.
   **/
  public Type getTypeWithName(String name) {
    return new AmbiguousNameType(this, name);
  }

  /**
   * Returns a type identical to <type>, but with <dims> more array
   * dimensions.  If dims is < 0, array dimensions are stripped.
   **/
  public Type extendArrayDims(Type type, int dims) {
    if (dims == 0) {
	return type;
    }
    else if (dims < 0) {
	if (type instanceof ArrayType) {
	  return extendArrayDims(((ArrayType) type).getBaseType(), dims+1);
	}
	else {
	  throw new InternalCompilerError("Cannot strip dimensions of non-array type " + type.getTypeString());
	}
    }
    else {
	return new ArrayType(this, type, dims);
    }
  }

  /**
   * Returns a canonical type corresponding to the Java Class object
   * theClass.  Does not require that <theClass> have a JavaClass
   * registered in this typeSystem.  Does not register the type in
   * this TypeSystem.  For use only by JavaClass implementations.
   **/
  public Type typeForClass(Class clazz) throws SemanticException
  {
    if( clazz == Void.TYPE) {
      return VOID_;
    }
    else if( clazz == Boolean.TYPE) {
      return BOOLEAN_;
    }
    else if( clazz == Byte.TYPE) {
      return BYTE_;
    }
    else if( clazz == Character.TYPE) {
      return CHAR_;
    }
    else if( clazz == Short.TYPE) {
      return SHORT_;
    }
    else if( clazz == Integer.TYPE) {
      return INT_;
    }
    else if( clazz == Long.TYPE) {
      return LONG_;
    }
    else if( clazz == Float.TYPE) {
      return FLOAT_;
    }
    else if( clazz == Double.TYPE) {
      return DOUBLE_;
    }
    else if( clazz.isArray()) {
      return new ArrayType( this, typeForClass( clazz.getComponentType()));
    }
    else {
      return resolver.findClass(clazz.getName());
    }
  }

  public TypeContext getEmptyContext(ClassResolver resolver) {
    return new EmptyContext(this, resolver);
  }

  public TypeContext getClassContext(ClassType type) throws SemanticException {
    return new ClassContext(type, resolver);
  }

  public TypeContext getClassContext(ClassType type, ClassResolver cr) throws SemanticException {
    return new ClassContext(type, cr);
  }

  public TypeContext getPackageContext(ClassResolver resolver, PackageType type) throws SemanticException {
    return new PackageContext(resolver, type);
  }

  public TypeContext getPackageContext(ClassResolver resolver, String name) throws SemanticException {
    return new PackageContext(resolver, new PackageType(this, name));
  }

  public java.util.Set getTypeEncoderRootSet(Type clazz) {
    /* by default, just clazz should be written out in full.  other
       classes are written as simple names. */
    return java.util.Collections.singleton(clazz);
  }

  public String translateArrayType(LocalContext c, ArrayType array){
      StringBuffer sb = new StringBuffer();
      sb.append(array.getBaseType().translate(c));
      sb.append("[]");
      return sb.toString();
  }

  public String translateClassType(LocalContext c, ClassType clazz){
      if (clazz.isAnonymous()) {
	  throw new InternalCompilerError(
	      "translate() called on anonymous class type " + clazz.getTypeString());
      }
      else if (clazz.isLocal()) {
	  return clazz.getShortName();
      }
      else {
	  ClassType container = clazz.getContainingClass();

	  if (clazz.isInner() && container.isAnonymous()) {
	      return clazz.getShortName();
	  }

	  // Return the short name if it is unique.
	  if (c != null) {
	      try {
		  Type t = c.getType(clazz.getShortName());

		  if (clazz.equals(t)) {
		      return clazz.getShortName();
		  }
	      }
	      catch (SemanticException e) {
	      }
	  }

	  if (clazz.isInner()) {
	      return container.translate(c) + "." + clazz.getShortName();
	  }
	  else {
	      return clazz.getFullName();
	  }
      }
  }

  public String translatePrimitiveType(LocalContext c, PrimitiveType prim){
      return prim.getTypeString();
  }

  public ParsedClassType newParsedClassType(ImportTable it,
      					    ClassType container) {
      return new ParsedClassType(this, it, container);
  }

  public List defaultPackageImports() {
      List l = new LinkedList();
      l.add("java.lang");
      return l;
  }
}

