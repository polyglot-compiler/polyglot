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
  public StandardTypeSystem(ClassResolver resolver) 
  {
    this.resolver = resolver;
    this.emptyImportTable = new ImportTable(new CompoundClassResolver());
  }

  /**
   * Initiazlies the type system to internal constants (which depend on the resolver 
   * being properly initialized) are themselves initialized.
   */
  public void initializeTypeSystem( ) throws TypeCheckException
  {
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

    if(ancestorType instanceof ClassType &&
       childType.equals( NULL_)) {
      return true;
    }

    if (ancestorType.equals(childType) ||
        ! (childType instanceof ClassType) ||
        ! (ancestorType instanceof ClassType) )
    {
      return false;
    }

    if (! ((ClassType)childType).getAccessFlags().isInterface()) {
      // If the child isn't an interface, check whether its supertype is or
      // descends from the ancestorType

      if ( ((ClassType)childType).equals ( OBJECT_))
        return false;

      ClassType parentType = (ClassType)((ClassType)childType).getSuperType();
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
    if (fromType instanceof ArrayType &&
          toType instanceof ArrayType) {
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
    else if (fromType instanceof ArrayType ||
          toType instanceof ArrayType) {
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
   * Checks whether a method, field or inner class within ctTarget with access flags 'flags' can
   * be accessed from Context context, where context is a class type.
   */
  public boolean isAccessible(ClassType ctTarget, AccessFlags flags, Context context) 
    throws TypeCheckException 
  {
    // check if in same class or public 
    if ( isSameType( ctTarget, context.inClass) ||
         flags.isPublic())
      return true;

    // check if context is an inner class of ctEnclosingClass, in which case protection doesnt matter
    if ( isEnclosed ( context.inClass, ctTarget))
      return true;
    // check if ctTarget is an inner of context, in which case protection doesnt matter either
    if ( isEnclosed ( ctTarget, context.inClass))
      return true;

    if ( ! (context.inClass instanceof ClassType))
    {
      throw new InternalCompilerError("Internal error: Context is not a Classtype");
    }

    ClassType ctContext = (ClassType)context.inClass;
    
    // check for package level scope. ( same package and flags has package level scope.
    if (ctTarget.getPackage().equals
        (ctContext.getPackage()) &&
        flags.isPackage())
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
    ClassType ct; 
    while ( (ct = tInner.getContainingClass()) != null &&
            ! ct.equals(tOuter));
    return ct != null;
  }

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  Otherwise, returns a String
   * describing the error.
   **/
  public Type checkAndResolveType(Type type, Context context) throws TypeCheckException {

    //    System.out.println( "resolving: " + type.getTypeString());

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

    //    System.out.println( "short? " + (TypeSystem.isNameShort(className)));


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
	ClassType parentType = (ClassType)inClass.getSuperType();
	ClassType outerType = inClass.getContainingClass();
	if (outerType != null) {
	  Context outerContext = new Context(emptyImportTable,
					     outerType, null);
	  resultFromOuter = checkAndResolveType(type, outerContext);
	}
	if (parentType != null) {
	  Context parentContext = new Context(emptyImportTable,
					      parentType, null);
          // System.out.println( "recursing to parent..."); 
          try
          {
            resultFromParent = checkAndResolveType(type, parentContext);
          }
          catch( TypeCheckException e) {}
          //  System.out.println( "back from parent!");
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
      //      context.table.dump();
      return context.table.findClass(className);
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

    try
    {
      result = (ClassType)checkAndResolveType(new AmbiguousType(this, prefix),
                                              context);
    }
    catch( TypeCheckException e) {}
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
   * Returns the fieldMatch named 'name' defined on 'type' visible in
   * context.  If no such field may be found, returns a fieldmatch
   * with an error explaining why. name and context may be null, in which case
   * they will not restrict the output.
   **/
  public FieldInstance getField(ClassType type, String name, Context context ) throws TypeCheckException
  {
    FieldInstance fi = null, fiEnclosing = null, fiTemp = null;
    ClassType tEnclosing = null;

    if (type != null) // then we have a starting point. don't have to perform a 2d search
    {
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
            throw new TypeCheckException(" Field \"" + name + "\" found in \"" + type.getFullName() + 
                                         "\", but with wrong access permissions.");
          }
        }
      }
      while ( (type = (ClassType)type.getSuperType()) != null);
      throw new TypeCheckException( "Field \"" + name + "\" not found");
    }
    else // type == null, ==> no starting point. so check superclasses as well as enclosing classes.
    {
      // check ourselves, first. this is because if a field is in this class, it cannot be ambiguous
      for (Iterator i = context.inClass.getFields().iterator(); i.hasNext(); )
      {
        fi = (FieldInstance)i.next();
        if (fi.getName().equals(name))
          // found it. can stop looking since guaranteed that it is not ambiguous
          return fi;
      }
      fi = null;

      // check the lineage of where we are
      try {  fi = getField( context.inClass, name, context); }
      catch ( TypeCheckException tce) { /* must have been something we couldnt access */ }

      boolean bFound = (fi != null);
      
      // now check all enclosing classes (this will also look for conflicts)
      tEnclosing = context.inClass.getContainingClass();
      while (tEnclosing != null)
      {
        try { fiTemp = getField(tEnclosing, name, context); }
        catch (TypeCheckException tce ) { /* must have been something we couldn't access */ }

        if (bFound && fiTemp != null)
        {
          throw new TypeCheckException("Ambiguous referenct to field \"" + name + "\"");
        }
        else if (fiTemp != null)
        {
          fi = fiTemp;
          bFound = true;
        }
      }
      if ( fi != null && fiEnclosing != null)
      {
        throw new TypeCheckException("Ambiguous referenct to field \"" + name + "\"");
      }
    }
    if ( fi!= null) return fi;
    if ( fiEnclosing != null) return fiEnclosing;


    // still no dice. check for a name like: {class}.{static_member}+.*
    ClassType result = null;
    String prefix = TypeSystem.getFirstComponent(name);
    String rest = TypeSystem.removeFirstComponent(name);
    
    try
    {
      result = (ClassType)checkAndResolveType(new AmbiguousType(this, prefix),
                                              context);
    }
    catch( TypeCheckException e) {}

    while (result == null && rest.length() > 0) {
      prefix = prefix + "." + TypeSystem.getFirstComponent(rest);
      rest = TypeSystem.removeFirstComponent(rest);
      try {
	result = resolver.findClass(prefix);
      } catch (NoClassException e) {}
    }
    if (result == null)
      throw new TypeCheckException( "Field \"" + name + "\" not found");
    // ah ha! we have a type. to work against.
    return getField ( result, rest, context );
    
  }

 /**
   * Requires: all type arguments are canonical.
   * 
   * Returns the MethodMatch named 'name' defined on 'type' visibile in
   * context.  If no such field may be found, returns a fieldmatch
   * with an error explaining why. Considers accessflags.
   **/
  public MethodTypeInstance getMethod(ClassType type, MethodType method, Context context)
    throws TypeCheckException
  {
    MethodTypeInstance mti = null, mtiEnclosing = null, mtiTemp = null;
    ClassType tEnclosing = null;

    if (type != null) // then we have a starting point. don't have to perform a 2d search
    {
      do 
      {
        for (Iterator i = type.getMethods().iterator(); i.hasNext() ; )
        {
          mti = (MethodTypeInstance)i.next();
          if ( methodCallValid( mti, method))
          {
            if ( isAccessible( type, mti.getAccessFlags(), context))
            {
              return mti;
            }
            throw new TypeCheckException(" Method \"" + method.getName() + "\" found in \"" + type.getFullName() + 
                                         "\", but with wrong access permissions.");
          }
        }
      }
      while ( (type = (ClassType)type.getSuperType()) != null);
      mti = null;
    }
    else // type == null, ==> no starting point. so check superclasses as well as enclosing classes.
    {
      // check ourselves, first. this is because if a field is in this class, it cannot be ambiguous
      for (Iterator i = context.inClass.getMethods().iterator(); i.hasNext(); )
      {
        mti = (MethodTypeInstance)i.next();
        if (methodCallValid(mti, method))
          // found it. can stop looking since guaranteed that it is not ambiguous
          return mti;
        mti = null;
      }

      // check the parent  lineage of where we are
      try {  mti = getMethod( context.inClass, method, context); }
      catch ( TypeCheckException tce) { /* must have been something we couldnt access */ }

      boolean bFound = (mti != null);
      
      // now check all enclosing classes (this will also look for conflicts)
      tEnclosing = context.inClass.getContainingClass();
      while (tEnclosing != null)
      {
        try { mtiTemp = getMethod(tEnclosing, method, context); }
        catch (TypeCheckException tce ) { /* must have been something we couldn't access */ }

        if (bFound && mtiTemp != null)
        {
          throw new TypeCheckException("Ambiguous referenct to method \"" + method.getName() + "\"");
        }
        else if (mtiTemp != null)
        {
          mti = mtiTemp;
          bFound = true;
        }
      }
      if ( mti != null && mtiEnclosing != null)
      {
        throw new TypeCheckException("Ambiguous referenct to method \"" + method.getName() + "\"");
      }
    }

    if ( mti != null) return mti;
    if ( mtiEnclosing != null) return mtiEnclosing;
    throw new TypeCheckException("No method \"" + method.getName() + "\" found with proper signature.");

  }

  /**
   * Returns the supertype of type, or null if type has no supertype.
   **/
  public ClassType getSuperType(ClassType type) throws TypeCheckException
  {
    return (ClassType)type.getSuperType();
  }

  /**
   * Returns an immutable list of all the interface types which type
   * implements.
   **/
  public List getInterfaces(ClassType type) throws TypeCheckException
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
  public Type leastCommonAncestor( Type type1, Type type2) throws TypeCheckException
  {
    if (( type1 instanceof PrimitiveType ) &&
        ( type2 instanceof PrimitiveType ))
    {
      if( ((PrimitiveType)type1).isBoolean()) {
        if( ((PrimitiveType)type2).isBoolean()) {
          return getBoolean();
        }
        else {
          throw new TypeCheckException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
        }

      }
      if( ((PrimitiveType)type2).isBoolean()) {
        throw new TypeCheckException( 
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
          throw new TypeCheckException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
        }
      }
      if( ((PrimitiveType)type2).isVoid()) {
        throw new TypeCheckException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
      } 
      
      return new PrimitiveType( this, Math.max ( 
                                        ((PrimitiveType) type1).getKind(), 
                                        ((PrimitiveType) type2).getKind() ));
    }
      
    if (!( type1 instanceof ClassType) ||
        !( type2 instanceof ClassType)) {
      throw new TypeCheckException( 
                       "No least common ancestor found. The type \"" 
                       + type1.getTypeString() + 
                       "\" is not compatible with the type \"" 
                       + type2.getTypeString() + "\"."); 
    }
    
    ClassType tSuper = (ClassType)type1;
    
    while ( ! type2.descendsFrom ( tSuper ) && tSuper != null) {
      tSuper = (ClassType)tSuper.getSuperType();
    }

    if ( tSuper == null) {
      throw new TypeCheckException( 
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
  private Type OBJECT_      ;
  private Type THROWABLE_   ;
  private Type ERROR_       ;
  private Type RTEXCEPTION_ ;
  private Type CLONEABLE_   ;
  
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
  public Type typeForClass(Class clazz) throws TypeCheckException
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
      return new ArrayType( this, typeForClass( clazz.getComponentType()), 1);
    }
    else {
      return resolver.findClass(clazz.getName());
    }
  }
}

