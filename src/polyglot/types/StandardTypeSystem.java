/*
 * StandardTypeSystem.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import jltools.util.InternalCompilerError;


/**
 * StandardTypeSystem
 *
 * Overview:
 *    A StandardTypeSystem is a universe of types, including all Java types.
 **/
public class StandardTypeSystem extends TypeSystem {

  // resolver should handle caching.
  public StandardTypeSystem(ClassResolver resolver) throws TypeCheckException
  {
    this.resolver = resolver;
    this.emptyImportTable = new ImportTable(resolver);
    
    // set up final objects:
    OBJECT_  = resolver.findClass( "java.lang.Object");
    THROWABLE_ = resolver.findClass( "java.lang.Throwable");
    ERROR_ = resolver.findClass( "java.lang.Error");
    RTEXCEPTION_ = resolver.findClass("java.lang.RuntimeException");
    CLONEABLE_ = resolver.findClass("java.lang.Cloneable");
  }

  ////
  // Functions for two-type comparison.
  ////

  /**
   * Returns true iff childType and ancestorType are distinct
   * classTypes, and childType descends from ancestorType.
   **/
  public boolean descendsFrom(Type childType, 
                              Type ancestorType) 
    throws TypeCheckException 
  {
    if ( childType instanceof AmbiguousType ||
         ancestorType instanceof AmbiguousType)
      throw new InternalCompilerError("Expected fully qualified classes.");
    if (ancestorType.equals(childType) ||
        ! (childType instanceof ClassType) ||
        ! (ancestorType instanceof ClassType) )
    {
      return false;
    }

    if (! ((ClassType)childType).getAccessFlags().isInterface()) {
      // If the child isn't an interface, check whether its supertype is or
      // descends from the ancestorType
      ClassType parentType = ((ClassType)childType).getSupertype();
      if (parentType.equals(ancestorType) ||
	  descendsFrom(parentType, ancestorType))
	return true;
    } else {
      // if it _is_ an interface, check whether the ancestor is Object.
      if (ancestorType.equals(OBJECT_))
	return true;
    } 

    // Next check interfaces.
    for(Iterator it = getInterfaces((ClassType)childType).iterator(); it.hasNext(); ) {
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
    throws TypeCheckException{

    if ( childType instanceof AmbiguousType ||
         ancestorType instanceof AmbiguousType)
      throw new InternalCompilerError("Expected fully qualified classes.");      

    // childType is primitive.
    if (childType instanceof PrimitiveType ||
	ancestorType instanceof PrimitiveType) 
      return false;

    // childType is array.
    if (childType instanceof ArrayType) {
      ArrayType  child = (ArrayType) childType;
      if (ancestorType instanceof ArrayType) {
	ArrayType ancestor = (ArrayType) ancestorType;
	if (child.getDimensions() == ancestor.getDimensions()) {
	  // Both types are arrays, of the same dimensionality.	
	  Type childbase = child.getBaseType();
	  Type ancestorbase = ancestor.getBaseType();
	  return isAssignableSubtype(childbase, ancestorbase);
	} else {
	  // Both type are arrays, of different dimensionality.
	  return (ancestor.getBaseType().equals(OBJECT_) &&
		  ancestor.getDimensions() < child.getDimensions());
	}
      } else {
	// childType is an array, but ancestorType not an array.
	return ancestorType.equals(OBJECT_);
      }
    } 
    
    // childType is null.
    if (childType instanceof NullType) 
      return true;

    // So childType is definitely a ClassType.
    if (! (ancestorType instanceof ClassType))
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
  public boolean isCastValid(Type fromType, Type toType)
    throws TypeCheckException
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
    if (fromType instanceof ArrayType) {
      if (toType instanceof ArrayType) {
	// FIXME: Make this iterative.
	Type fromBase = extendArrayDims((ArrayType)fromType,-1);
	Type toBase   = extendArrayDims((ArrayType)toType,-1);
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
    throws TypeCheckException
  {
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
      PrimitiveType ptFromType = (PrimitiveType) fromType;
      PrimitiveType ptToType = (PrimitiveType) toType;
      
      if (! ptFromType.isNumeric() || ! ptToType.isNumeric())       
	return false;

      // ...and numeric.
      switch (ptFromType.getKind()) 
	{
	  
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

  ////
  // Functions for one-type checking and resolution.
  ////
  
  /**
   * Returns true iff <type> is a canonical (fully qualified) type.
   **/
  public boolean isCanonical(Type type) throws TypeCheckException {
    return type.isCanonical();
  }
      
  /**
   * Checks whether a method, field or inner class  within tEnclosingClass with access flags 'flags' can
   * be accessed from Context context, where context is a class type.
   */
  public boolean isAccessible(ClassType ctEnclosingClass, AccessFlags flags, Context context) 
    throws TypeCheckException 
  {
    // check if in same class or public 
    if ( isSameType( ctEnclosingClass, context.inClass) ||
         flags.isPublic())
      return true;

    if ( ! (context.inClass instanceof ClassType))
    {
      throw new InternalCompilerError("Internal error: Context is not a Classtype");
    }

    ClassType ctContext = (ClassType)context.inClass;
    
    // check for package level scope. ( same package and flags has package level scope.
    if (ctEnclosingClass.getPackage().equals
        (ctContext.getPackage()) &&
        flags.isPackage())
      return true;
    
    // protected
    if ( ctContext.descendsFrom( ctEnclosingClass ) &&
         flags.isProtected())
      return true;
    
    // else, 
    return false;
  }

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  Otherwise, returns a String
   * describing the error.
   **/
  public Type checkAndResolveType(Type type, Context context) throws TypeCheckException {
    if (type.isCanonical()) return type;
    if (type instanceof ArrayType) {
      ArrayType at = (ArrayType) type;
      Type base = at.getBaseType();
      int dims = at.getDimensions();
      Type result = checkAndResolveType(base, context);
      return new ArrayType(this, (Type)result, dims);
    }
    
    if (! (type instanceof AmbiguousType)) 
      throw new InternalCompilerError("Found a non-canonical, non-array, non-ambiguous type.");

    // We have a class type on our hands.
    String className = ((AmbiguousType) type).getTypeString();

    // Find the context.
    ClassType inClass = context.inClass;

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
	ClassType parentType = inClass.getSupertype();
	ClassType outerType = inClass.getContainingClass();
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
	  throw new TypeCheckException ("Found " + className + " in both outer and parent.");
	} else if (resultFromOuter instanceof ClassType) {
	  return resultFromOuter;
	} else if (resultFromParent instanceof ClassType) {
	  return resultFromParent;
	}
      }

      // STEP 3
      // Check the import table.  Default to the null package.
      try 
      {  return context.table.findClass(className); }
      catch (NoClassException nce) 
      { throw new TypeCheckException(" No \"" + className + "\" found in context or import table."); }

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
    ClassType result = null;
    String prefix = TypeSystem.getFirstComponent(className);
    String rest = TypeSystem.removeFirstComponent(className);
    result = (ClassType)checkAndResolveType(new AmbiguousType(this, prefix), context);
    while (result == null && rest.length() > 0) {
      prefix = prefix + "." + TypeSystem.getFirstComponent(rest);
      rest = TypeSystem.removeFirstComponent(rest);
      try {
	result = resolver.findClass(prefix);
      } catch (NoClassException e) {}
    }
    if (result == null)
      throw new TypeCheckException( "No class found for " + className );
    

    // Type outer = result;
    // JavaClass resultClass = getClassForType(outer);
    while (rest.length() > 0) {
      String innerName = TypeSystem.getFirstComponent(rest);
      result = result.getInnerNamed(innerName);
      if (result == null)
	throw new TypeCheckException ("Class " + prefix + " has no inner class named " + innerName);
      prefix = prefix + "." + innerName;
      rest = TypeSystem.removeFirstComponent(rest);
    }
    return result;
  }

  ////
  // Various one-type predicates.
  ////
  /**
   * Returns true iff an object of type <type> may be thrown.
   **/
  public boolean isThrowable(Type type) throws TypeCheckException {
    return descendsFrom(type,THROWABLE_) || type.equals(THROWABLE_);
  }
  /**
   * Returns true iff an object of type <type> may be thrown by a method
   * without being declared in its 'throws' clause.
   **/
  public boolean isUncheckedException(Type type) throws TypeCheckException {
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
  //  public Iterator getFieldsForType(Type type) 
  //  {
  //    return getField(type, null, null);
  //  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable iterator of all the MethodMatches defined on
   * type (if any).  The iterator is guaranteed to yield methods
   * defined on subclasses before those defined on superclasses.
   **/  

//  public Iterator getMethodsForType(Type type)
//  {
//    return getMethod(type, null, null);
//  }
  

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns an immutable iterator of all the FieldMatches named 'name' defined
   * on type (if any).  If 'name' is null, matches all.  The iterator is guaranteed 
   * to yield fields defined on subclasses before those defined on superclasses.
   **/
//  public Iterator getFieldsNamed(Type type, String name)
//  {
//    return getField( type, name, null);
//  }
  
  
  /**
   * Requries all type are canonical.
   * 
   * Returns an immutable iterator of all the MethodMatches named 'name' defined
   * on type (if any).  If 'name' is null, mathces all. The iterator is guaranteed
   * to yield methods defined on subclasses before those defined on superclasses.
   */
//  public  Iterator getMethodsNamed(Type type, String name)
//  {
//    return getMethod(type, name, null);
//  }

  /**
   * Requires: all type arguments are canonical.
   *
   * Returns the fieldMatch named 'name' defined on 'type' visible in
   * context.  If no such field may be found, returns a fieldmatch
   * with an error explaining why. name and context may be null, in which case
   * they will not restrict the output.
   **/
  public FieldInstance getField(ClassType type, String name, Context context, boolean bIsThis ) throws TypeCheckException
  {
    /*    if (! type.isCanonical() )
      throw new TypeCheckException("Type: \"" + type + "\" not a canoncial type.");
    
    LinkedList worklistEnclosingTypes = new LinkedList();
    Type tResult;
    List lTempResult; // Typed list containing FieldInstances
    Type tEnclosing;

    // check ourselves:
    lTempResults = type.getFields();
    for (Iterator i = lTempResult.listIterator(); i.hasNext(); )
    {
      FieldInstance fi = (FieldInstance)i.next();
      if (fi.getName().equals(name))
        // found it. can stop looking since guaranteed that it is not looking.
        return fi;
    }

    // now look through those classes that type descended from
    while (type != null)
    {
      lTempResult = type.getFields();
      for (Iterator i = lTempResult.listIterator(); i.hasNext(); )
      {
        FieldInstance fi = (FieldInstance)i.next();
        // only "find"those fields which match our input criteria, namely, 
        //   - make sure that the names match
        //   - make sure that we have proper access from this context.
        if ( (fi.getName().equals (name)))
          if ( isAccessible( type, fi.getAccessFlags(),  context))
          {
            tResult = fi;
          }
          else 
            throw new TypeCheckError( "Cannot access 

      }
      // if we have an enclosing class, throw it in the worklist.
      tEnclosing = type.getContainingClass();
      if (tEnclosing != null && ! worklistEnclosingTypes.contains(tEnclosing))
        worklistEnclosingTypes.add( tEnclosing );
      type = getClassForType( class_.getSupertype());
    }

    // now go through our worklist.
    for (Iterator iWorklist = worklistEnclosingTypes.iterator(); iWorklist.hasNext() ; )
    {
      Iterator iResult = getFieldsForType( (Type)iWorklist.next() );
      // and add 'em to our result list.
      for (; iResult.hasNext() ; )
      {
        lResults.add ( iResult.next() );
      }
    }
    return lResults.iterator();
    */
    return null;
  }

 /**
   * Requires: all type arguments are canonical.
   * 
   * Returns the MethodMatch named 'name' defined on 'type' visibile in
   * context.  If no such field may be found, returns a fieldmatch
   * with an error explaining why. Considers accessflags.
   **/
  public Iterator getMethod(ClassType type, String name, Context context)
    throws TypeCheckException
  {
    /*    // FIXME: nks: deprecated

    if (! type.isCanonical() )
      throw new TypeCheckException("Type: \"" + type + "\" not a canoncial type.");
    
    LinkedList worklistEnclosingTypes = new LinkedList();
    List lResults = new LinkedList();
    List lTempResult; // Typed list containing MethodTypeIstances
    Type tEnclosing;

    // first add all that we descend from
    while (type != null)
    {
      lTempResult = type.getMethods();
      // wrap all the MethodTypeInstances in MethodMatches, and add them to our list.
      for (Iterator i = lTempResult.listIterator(); i.hasNext(); )
      {
        MethodTypeInstance mti =  (MethodTypeInstance)i.next();
        // only add those methods which match our input criteria, namely, 
        //   - make sure that the names match (if specified) and 
        //   - make sure that we have proper access from this context (if specified)
        if ( (mti.getName().equals( name) || name == null) &&
             (context == null || isAccessible( type, 
                                               mti.getAccessFlags(), context)))
          lResults.add( new MethodMatch ( class_.getType(), mti));
      }
      // if we have an enclosing class, throw it in the worklist.
      tEnclosing = type.getContainingClass();
      if (tEnclosing != null && ! worklistEnclosingTypes.contains(tEnclosing))
        worklistEnclosingTypes.add( tEnclosing );
      type = getClassForType( type.getSupertype());
    }

    // now go through our worklist.
    for (Iterator iWorklist = worklistEnclosingTypes.iterator(); iWorklist.hasNext() ; )
    {
      Iterator iResult = getMethodsForType( (Type)iWorklist.next() );
      // and add 'em to our result list.
      for (; iResult.hasNext() ; )
      {
        lResults.add ( iResult.next() );
      }
    }
    return lResults.iterator();
    */
    return null;
  }

  /**
   * Returns the supertype of type, or null if type has no supertype.
   **/
  public ClassType getSuperType(ClassType type) throws TypeCheckException
  {
    return (ClassType)type.getSupertype();
  }

  /**
   * Returns an immutable list of all the interface types which type
   * implements.
   **/
  public List getInterfaces(ClassType type) throws TypeCheckException
  {
    return type.getInterfaces();
  }

  ////
  // Functions for method testing.
  ////
  /**
   * Returns true iff <type1> is the same as <type2>.
   **/
  public boolean isSameType(MethodType type1, MethodType type2) 
  {
    return ( type1.equals(type2));
  }

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
   * Returns whether the arguments for MethodType call can call MethodTypeInstance prototype
   */
  public boolean methodCallValid( MethodTypeInstance prototype, MethodType call) 
    throws TypeCheckException
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
      if ( ! descendsFrom ( (Type)iArgumentTypesCall.next(), (Type)iArgumentTypesProto.next() ) )
        return false;
    return true;
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
  public MethodTypeInstance getMethod(ClassType type, MethodType method, 
                                      Context context, boolean isThis)
    throws TypeCheckException 
  {
    /*    // we first get the list of methods which match our type, method name and 
    // are accessible from our context.  We make use of the fact that 
    // the methods come back such taht those in the subclass come before those in a 
    // super class.
    MethodMatch match = null, temp = null;
    for ( Iterator i = getMethod( type, method.getName() , context)   ; i.hasNext() ; )
    {
      temp = (MethodMatch)temp;
      // if isThis is true, make sure we don't check enclosing classes (checking super
      // classes is ok, though) & that the type signatures are compatible
      if (   ( ( !isThis ) || context.inClass.descendsFrom ( temp.onClass ) ) &&
             methodCallValid( temp.method, method) )
        // new ==> this is our candidation.  keep looking to make sure that this  isn't
        //         ambiguous
        if (match == null)
          match = temp;
        else 
          // we have a match already. if our temp refines the signature of our current match
          // (from the subclass), then we have an ambiguous situation.
          if ( methodCallValid( temp.method, match.method) )
            throw new TypeCheckException(" The call is ambiguous; more than 1 prototype will match");
    }
    return match;
    */
    return null;
  }

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
//public MethodMatch getMethodInClass(Type type, MethodType method, 
//                                      Context context, boolean isThis)
//  {
    //FIXME: implement
//    return null;
//  }

  /**
   * As above, except only returns a match if the argument types are identical,
   * and disregards context.
   **/
//  public MethodMatch getExactMethod(Type type, MethodType method)
//  {
    //FIXME: implement
//    return null;
//  }

//  public MethodMatch getExactMethodInClass(Type type, MethodType method)
//  {
//    //FIXME: implement
//    return null;
//  }

  ////
  // Functions for type->class mapping.
  ////
  /**
   * Requires: all arguments are canonical.
   *
   * Returns the JavaClass object corresponding to a given type, or null
   * if there is none.
   **/
//  public JavaClass getClassForType(Type type) {
//    if (! (type instanceof ClassType)) return null;
//    String fullName = ((ClassType) type).getName();
//    try { return resolver.findClass(fullName); }
//    catch (NoClassException nce) 
//    { return null; }
//  }

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
  private final Type OBJECT_      ;
  private final Type THROWABLE_   ;
  private final Type ERROR_       ;
  private final Type RTEXCEPTION_ ;
  private final Type CLONEABLE_   ;
  
  protected ClassResolver resolver; //Should do its own caching.
  protected ImportTable emptyImportTable;

  /**
   * Returns a non-canonical type object for a class type whose name
   * is the provided string.  This type may not correspond to a valid
   * class.
   **/
  public AmbiguousType getTypeWithName(String name) {
    return new AmbiguousType(this, name);
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
      base = t.getBaseType();
      newDims += t.getDimensions();
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
//  public ClassType typeForClass(JavaClass theClass)
//  {
//    return (ClassType)theClass.getType();
//  }

  /**
   * Returns a canonical type corresponding to the Java Class object
   * theClass.  Does not require that <theClass> have a JavaClass
   * registered in this typeSystem.  Does not register the type in
   * this TypeSystem.  For use only by JavaClass implementations.
   **/
  public  ClassType typeForClass(Class clazz) throws TypeCheckException
  {
    return resolver.findClass(clazz.getName());
  }
}

