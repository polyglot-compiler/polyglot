/*
 * JavaClassImpl.java
 */

package jltools.types;

import java.util.List;
import jltools.util.TypedList;
import jltools.util.AnnotatedObject;

/**
 * JavaClassImpl
 *
 * Overview: 
 *    A JavaClassImpl is used to implement non-lazy Java classes: ones where
 *    information is computed once, rather than on demand. 
 **/
public abstract class JavaClassImpl extends AnnotatedObject 
  implements JavaClass {
  
  public String getFullName()         { return fullName; }
  public String getShortName()        { return shortName; }
  public String getPackage()          { return packageName; }
  public Type getType()               { return classType; }

  public List getMethods()              { return methods; }
  public List getFields()               { return fields; }
  public List getInterfaces()           { return interfaces; }

  public AccessFlags getAccessFlags()   { return flags; }
  public Type getSupertype()            { return superType; } 
  public boolean isInner()              { return isInner; } 
  public boolean isAnonymous()          { return isAnonymous; }
  public Type getContainingClass()      { return containingClass; }
  public String getInnerName()          { return innerName; }
  public List getInnerClasses()         { return innerClasses; }

  ////
  // All or most of the information below must be initialized before
  // we can use this class.
  ////

  ////
  // Metadata
  ////
  // The associated TypeSystem.
  protected TypeSystem ts;
  
  ////
  // Names
  ////
  // The package name.
  protected String packageName;
  // The full name, including package and outers.
  protected String fullName;
  // The short name, not including outers.  
  protected String shortName;

  ////
  // Typing info
  ////
  // The type for this class.
  protected ClassType classType;
  // The supertype.  (null for JLO)
  protected ClassType superType;
  // The TypedList of interface types.
  protected TypedList interfaces;
  // The access flags for this class.
  protected AccessFlags flags;

  ////
  // Members
  ////
  protected TypedList methods;
  protected TypedList fields;

  ////
  // Inner info
  ////
  protected boolean isInner;
  protected boolean isAnonymous;
  protected Type containingClass;
  protected String innerName;
  protected TypedList innerClasses;
}

