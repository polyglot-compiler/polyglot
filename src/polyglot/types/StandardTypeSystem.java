/*
 * StandardTypeSystem.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.List;

/**
 * StandardTypeSystem
 *
 * Overview:
 *    A StandardTypeSystem is a universe of types, including all Java types.
 **/
public abstract class StandardTypeSystem extends TypeSystem {

  // FIXME: Documentation.
  // resolver should handle caching.
  public StandardTypeSystem(ClassResolver resolver) {
    this.resolver = resolver;
    this.emptyImportTable = new ImportTable(resolver);
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
  public boolean descendsFrom(JavaClass childClass, 
			      JavaClass ancestorClass) {
    return descendsFrom(typeForClass(childClass),
			typeForClass(ancestorClass));
  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff childType and ancestorType are distinct
   * classTypes, and childType descends from ancestorType.
   **/
  public boolean descendsFrom(Type childType, 
			      Type ancestorType) {
    if (ancestorType.equals(childType) || 
	! (childType instanceof ClassType) || 
	! (ancestorType instanceof ClassType))
      return false;

    JavaClass childClass = resolver.findClass(childType);

    if (! childClass.getAccessFlags().isInterface()) {
      // If the child isn't an interface, check whether its supertype is or
      // descends from the ancestorType
      JavaClass parentType = getSuperType(childType);
      if (parentType.equals(ancestorType) ||
	  descendsFrom(parentType, ancestorType))
	return true;
    } else {
      // if it _is_ an interface, check whether the ancestor is Object.
      if (ancestorType.equals(OBJECT_))
	return true;
    } 

    // Next check interfaces.
    for(Iterator it = getInterfaces(childType).iterator(); it.hasNext(); ) {
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
				     Type ancestorType) {
    ////
    // childType is primitive.
    ////
    if (childType instanceof PrimitiveType ||
	ancestorType instanceof PrimitiveType) 
      return false;

    ////
    // childType is array.
    ////
    if (childType instanceof ArrayType) {
      ArrayType  child = (ArrayType) childType;
      if (ancestorType instanceof ArrayType) {
	ArrayType ancestor = (ArrayType) ancestorType;
	if (child.getDimensions() == ancestor.getDimensions()) {
	  // Both types are arrays, of the same dimensionality.	
	  Type childbase = childType.getBaseType();
	  Type ancestorbase = ancestor.getBaseType();
	  return isAssignableSubtype(childBase, ancestorBase);
	} else {
	  // Both type are arrays, of different dimensionality.
	  return (ancestor.getBaseType().equals(OBJECT_) &&
		  ancestor.detDimensions() < child.getDimensions());
	}
      } else {
	// childType is an array, but ancestorType not an array.
	return ancestorType.equals(OBJECT_);
      }
    } 
    
    ////
    // childType is null.
    ////
    if (childType instanceof NullType) 
      return true;

    ////
    // So childType is definitely a ClassType.
    ////
    if (! ancestorType instanceof ClassType)
      return false;
    
    return (childType.equals(ancestorType) || 
	    descendsFrom(childType, ancestorType));    
  }
  /**
   * Requires: all type arguments are canonical.  ToType is not a NullType.
   *
   * Returns true iff a cast from fromType to toType is valid; in other
   * words, some non-null members of fromType are also members of toType.
   **/
  public boolean isCastValid(Type fromType, Type toType) {
    ////
    // Are they distinct?
    ////
    if (fromType.equals(toType)) return true;
    
    ////
    // Are they primitive?
    ////
    if (fromType instanceof PrimitiveType) {
      if (! (toType instanceof PrimitiveType)) 
	return false;

      // Distinct primitive types are only convertable if type are numeric.
      if (fromType.isNumeric() && toType.isNumeric()) return true;
      
      return false;
    }
    if (toType instanceof PrimitiveType) return false;
  
    if (fromType instanceof NullType) return true;

    ////
    // Array cases.
    ////
    if (fromType instanceof ArrayType) {
      if (toType instanceof ArrayType) {
	// FIXME: Make this iterative.
	Type fromBase = extendArrayDims(fromType,-1);
	Type toBase   = extendArrayDims(toType,-1);
	if (fromBase instanceof PrimitiveType) {
	  return toBase.equals(fromBase);
	} else if (toBase instanceof PrimitiveType) {
	  return false;
	}	
	// Both bases are reference types.
	return isCastValid(fromBase, toBase);
      }
      // From an array to a non-array.
      return toType.equals(CLONEABLE_) || toType.equals(OBJECT_);
    }

    ////
    // From and to are neither primitive nor an array. They are distinct.
    ////    
    JavaClass fromClass = getClassForType(fromType);
    JavaClass toClass = getClassForType(fromType);    
    boolean fromInterface = fromClass.getAccessFlags().isInterface();
    boolean toInterface   =   toClass.getAccessFlags().isInterface();
    boolean fromFinal     = fromClass.getAccessFlags().isFinal();
    boolean toFinal       =   toClass.getAccessFlags().isFinal();

    ////
    // This is taken from Section 5.5 of the JLS.
    ////
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
	// FIXME: This should check whether they have any conflicting
	// methods.
      }
    }
  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an implicit cast from fromType to toType is valid;
   * in other words, every member of fromType is member of toType.
   **/
  public boolean isImplicitCastValid(Type fromType, Type toType) {
    // TODO: read JLS 5.1 to be sure this is valid.

    if (fromType.equals(toType)) return true;
    
    ////
    // Types are distinct.
    ////

    if (fromType instanceof PrimitiveType) {
      if (! (toType instanceof PrimitiveType)) return false;
      ////
      // Both types are primitive...
      ////
      PrimitiveType child = (PrimitiveType) childType;
      PrimitiveType ancestor = (PrimitiveType) ancestorType;
      
      if (! child.isNumeric() || ! ancestor.isNumeric())       
	return false;

      // ...and numeric.
      switch (fromType.kind) 
	{
	  
	case PrimitiveType.BYTE:
	case PrimitiveType.SHORT:
	case PrimitiveType.CHAR:
	  if (this.kind == PrimitiveType.INT) return true;
	case PrimitiveType.INT:
	  if (this.kind == LONG) return true;
	case PrimitiveType.LONG:
	  if (this.kind == FLOAT) return true;
	case PrimitiveType.FLOAT:
	  if (this.kind == DOUBLE) return true;
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
   * Requires: all type arguments are canonical.
   *
   * Returns true iff type1 and type2 are the same type.
   **/
  public boolean isSameType(Type type1, Type type2) {
    return type1.equals(type2);
  }

  ////
  // Functions for one-type checking and resolution.
  ////
  
  /**
   * Returns true iff <type> is a canonical (fully qualified) type.
   **/
  public boolean isCanonical(Type type) {
    return type.isCanonical();
  }
      
  /**
   * Tries to return the canonical (fully qualified) form of <type> in
   * the provided context, which may be null.  Returns null if no such
   * type exists.
   **/
  public Type getCanonicalType(Type type, Context context) {
    Object res = checkAndResolveType(type, context);
    return (res instanceof String) ? null : (Type) res;
  }
  /**
   * Checks whether <type> is a valid type in the given context,
   * which may be null.  Returns a description of the error, if any.
   **/
  public String checkTypeOk(Type type, Context context) {
    Object res = checkAndResolveType(type, context);
    return (res instanceof String) ? (String) res : null;
  }
  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  Otherwise, returns a String
   * describing the error.
   **/
  public Object checkAndResolveType(Type type, Context context) {
    if (type.isCanonical()) return type;
    if (type instanceof ArrayType) {
      ArrayType at = (ArrayType) type;
      Type base = at.getBaseType();
      int dims = at.getDimensions();
      Object result = checkAndResolveType(base, context);
      if (result instanceof Type)
	return new ArrayType(this, result, dims);
      else
	return result;
    }
    
    if (! (type instanceof ClassType)) 
      throw new Error("Found a non-canonical, non-array, non-class type.");

    // We have a class type on our hands.
    String className = ((ClassType) type).getName();

    // Find the context.
    JavaClass inClass =( context.inClass == null ? null : 
			 getClassForType(context.inClass) );

    // Is the name short?
    if (TypeSystem.isNameShort(className)) {
      // Sun's Java compiler seems to follow these steps.  The spec is
      // a bit hazy here, so we'll use the compiler as a reference.

      if (inClass != null) {
	// STEP 1
	// First, we see if the current class has any inners by that name.
	Type innerType = inClass.getInnerNamed(className);
	if (innerType != null) return innerType;

	// STEP 2
	// Now we see whether the outer class or the parent class have
	// any inners by that name.  If they _both_ do, that's an error.
	Type resultFromOuter = null;
	Type resultFromParent = null;
	Type parentType = inClass.getSuperType();
	Type outerType = inClass.getContainingClass();
	if (outerType != null) {
	  Context outerContext = new Context(emptyImportTable,
					     outerType, null);
	  resultFromOuter = checkAndResolveType(type, outerContext);
	}
	if (parentType != null) {
	  Context parentContext = new Context(emptyImportTable,
					      parentType, null);
	  resultFromParent = checkAndResolveType(type, parentContext);
	}
	if ((resultFromOuter instanceof ClassType) &&
	    (resultFromParent instanceof ClassType)) {
	  // FIXME: Better error message needed.
	  return "Found " + className + " in both outer and parent.";
	} else if (resultFromOuter instanceof ClassType) {
	  return resultFromOuter;
	} else if (resultFromParent instanceof ClassType) {
	  return resultFromParent;
	}
      }

      // STEP 3
      // Check the import table.  Default to the null package.
      return Context.table.findClass(className);
    }

    // It looks like we've got a long name.  It can be of only one of
    // the following forms:
    //          class{.inner}+
    //          {package.}+class
    //          {package.}+class{.inner}+
    //
    // Our strategy is to first check to see if the first component is
    // a class.  If it is, then we look for the appropriate inner.
    // Otherwise, we try to find the shortest possible prefix of the
    // name that is not an inner class, and then look for inners there.
    
    // 
    // We'll try to set <result> equal to the type of the outermost
    // class, <prefix> equal to that class's name, and <rest> equal to
    // the leftover parts.
    Type result = null;
    String prefix = TypeSystem.getFirstComponent(className);
    String rest = TypeSystem.removeFirstComponent(className);
    result = getCanonicalType(new ClassType(this, prefix, false), context);
    if (result instanceof String) 
      result = null;
    while (result == null && rest.size() > 0) {
      prefix = prefix + "." + TypeSystem.getFirstComponent(rest);
      rest = TypeSystem.removeFirstComponent(rest);
      try {
	result = resolver.findClass(prefix).getType();
      } catch (NoClassException e) {}
    }
    if (result == null)
      return "No class found for " + className;
    
    Type outer = result;
    JavaClass resultClass = getClassForType(outer);
    while (rest.size() > 0) {
      String innerName = TypeSystem.getFirstComponent(rest);
      result = resultClass.getInnerNamed(innerName);
      if (result == null)
	return "Class " + prefix + " has no inner class named " + innerName;
      resultClass = getClassForType(result);
      prefix = prefix + "." + innerName;
      rest = TypeSystem.removeFirstComponent(rest);
    }
    return result;
  }

  ////
  // Various one-type predicates.
  ////
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an object of type <type> may be thrown.
   **/
  public boolean isThrowable(Type type) {
    return descendsFrom(type,THROWABLE_) || type.equals(THROWABLE_);
  }
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns true iff an object of type <type> may be thrown by a method
   * without being declared in its 'throws' clause.
   **/
  public boolean isUncheckedException(Type type) {
    return descendsFrom(type,ERROR_) || type.equals(ERROR_) || 
      descendsFrom(type,RTEXCEPTION_) || type.equals(RTEXCEPTION_);
  }

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
  public Iterator getFieldsForType(Type type) {
    JavaClass class_ = classForType(type);
    // TODO
  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable iterator of all the MethodMatches defined on
   * type (if any).  The iterator is guaranteed to yield methods
   * defined on subclasses before those defined on superclasses.
   **/  
  public Iterator getMethodsForType(Type type)
  {
    //FIXME: implement
    return null;
  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable iterator of all the FieldMatches named 'name' defined
   * on type (if any).  The iterator is guaranteed to yield methods
   * defined on subclasses before those defined on superclasses.
   **/
  public Iterator getFieldsNamed(Type type, String name)
  {
    //FIXME: implement
    return null;
  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns the fieldMatch named 'name' defined on 'type' visible in
   * context.  If no such field may be found, returns a fieldmatch
   * with an error explaining why.
   **/
  public Iterator getField(Type type, String name, Context context)
  {
    //FIXME: implement
    return null;
  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable of all the MethodMatches named 'name' defined on
   * type (if any).  The iterator is guaranteed to yield methods
   * defined on subclasses before those defined on superclasses.
   **/  
  // FIXME
  //public Iterator getMethodsNamed(Type type, String name);
  public abstract Iterator getMethodsNamed(Type type, String name);
  /**
   * Requires: all type arguments are canonical.
   *
   * Returns the supertype of type, or null if type has no supertype.
   **/
  // FIXME
  //public Type getSuperType(Type type);
  public abstract Type getSuperType(Type type);

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable list of all the interface types which type
   * implements.
   **/
  // FIXME
  //public List getInterfaces(Type type);
  public abstract List getInterfaces(Type type);


  ////
  // Functions for method testing.
  ////
  /**
   * Returns true iff <type1> is the same as <type2>.
   **/
  public boolean isSameType(MethodType type1, MethodType type2)
  {
    //FIXME: implement
    return null;
  }

  /**
   * Returns true iff <type1> has the same arguments as <type2>
   **/
  public boolean hasSameArguments(MethodType type1, MethodType type2)
  {
    //FIXME: implement
    return null;
  }

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
  // FIXME
  //public MethodMatch getMethod(Type type, MethodType method, 
  public abstract MethodMatch getMethod(Type type, MethodType method, 
					Context context, boolean isThis);

  /**
   * If an attempt to call a method of type <method> on <type> would
   * be successful, and the method would match on the given <type>,
   * returns the actual MethodMatch for the method that would be
   * called.  Otherwise returns a MethodMatch with an error string
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
  public MethodMatch getMethodInClass(Type type, MethodType method, 
					      Context context, boolean isThis)
  {
    //FIXME: implement
    return null;
  }

  /**
   * As above, except only returns a match if the argument types are identical,
   * and disregards context.
   **/
  public MethodMatch getExactMethod(Type type, MethodType method)
  {
    //FIXME: implement
    return null;
  }

  public MethodMatch getExactMethodInClass(Type type, MethodType method)
  {
    //FIXME: implement
    return null;
  }

  ////
  // Functions for type->class mapping.
  ////
  /**
   * Requires: all arguments are canonical.
   *
   * Returns the JavaClass object corresponding to a given type, or null
   * if there is none.
   **/
  public JavaClass getClassForType(Type type) {
    if (! (type instanceof ClassType)) return null;
    String fullName = ((ClassType) type).getName();
    return resolver.findClass(fullName);
  }

  ////
  // Functions which yield particular types.
  ////
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
  public Type getThrowable() {return THROWABLE_; }

  private final Type NULL_    = new NullType(this);
  private final Type VOID_    = new PrimitiveType(this, PrimitiveType.VOID);
  private final Type BOOLEAN_ = new PrimitiveType(this, PrimitiveType.BOOLEAN);
  private final Type CHAR_    = new PrimitiveType(this, PrimitiveType.CHAR);
  private final Type BYTE_    = new PrimitiveType(this, PrimitiveType.BYTE);
  private final Type SHORT_   = new PrimitiveType(this, PrimitiveType.SHORT);
  private final Type INT_     = new PrimitiveType(this, PrimitiveType.INT);
  private final Type LONG_    = new PrimitiveType(this, PrimitiveType.LONG);
  private final Type FLOAT_   = new PrimitiveType(this, PrimitiveType.FLOAT);
  private final Type DOUBLE_  = new PrimitiveType(this, PrimitiveType.DOUBLE);
  private final Type OBJECT_  = new ClassType(this, "java.lang.Object", true);
  private final Type THROWABLE_ = 
    new ClassType(this, "java.lang.Throwable", true);
  private final Type ERROR_ = 
    new ClassType(this, "java.lang.Error", true);
  private final Type RTEXCEPTION_ = 
    new ClassType(this, "java.lang.RuntimeException", true);
  private final Type CLONEABLE_ = 
    new ClassType(this, "java.lang.Cloneable", true);
  
  protected ClassResolver resolver; //Should do its own caching.
  protected ImportTable emptyImportTable;

  /**
   * Returns a non-canonical type object for a class type whose name
   * is the provided string.  This type may not correspond to a valid
   * class.
   **/
  public ClassType getTypeWithName(String name) {
    return new ClassType(this, name, false);
  }
  /**
   * Returns a type identical to <type>, but with <dims> more array
   * dimensions.  If dims is < 0, array dimensions are stripped.
   **/
  public Type extendArrayDims(Type type, int dims) {
    Type base = type;
    int newDims = dims;
    if (type instanceof ArrayType) {
      ArrayType t = (ArrayType) type;
      base = type.getBaseType();
      newDims += type.getDimensions();
    }
    if (newDims == 0) 
      return base;
    else
      return new ArrayType(this, base, newDims); // May throw error.
  }
  /**
   * Returns a canonical type corresponding to the Java Class object
   * theClass.  Does not require that <theClass> have a JavaClass
   * registered in this typeSystem.  Does not register the type in
   * this TypeSystem.  For use only by JavaClass implementations.
   **/
  public ClassType typeForClass(Class theClass)
  {
    //FIXME: implement
    return null;
  }

}

