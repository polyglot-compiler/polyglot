package jltools.types;

import jltools.util.*;

import java.io.*;
import java.util.*;


/**
 * ParsedClassType
 *
 * Overview: 
 * A ParsedClassType represents a information that has been parsed (but not
 * necessarily type checked) from a .java file.
 **/
public class ParsedClassType extends ClassTypeImpl 
{
  static final long serialVersionUID = 5725500800448862634L;

  ImportTable it;
  
  protected ParsedClassType()
  {
    super();
  }

  public ParsedClassType( TypeSystem ts, ImportTable it)
  {
    this( ts, it, null);
  }

  public ParsedClassType( TypeSystem ts, ImportTable it, ClassType containingClass)
  {
    super( ts);
    this.it = it;
    interfaces = new TypedList( new LinkedList(), Type.class, false);
    methods = new TypedList( new LinkedList(), MethodTypeInstance.class,
                             false);
    fields = new TypedList( new LinkedList(), FieldInstance.class, false);
    innerClasses = new TypedList( new LinkedList(), Type.class, false);
    this.containingClass = containingClass;
  }

  public ImportTable importTable() {
    return it;
  }

  public void setPackageName( String packageName)
  {
    this.packageName = packageName;
  }

  public void setFullName( String fullName)
  {
    this.fullName = fullName;
  }

  public void setShortName( String shortName)
  {
    this.shortName = shortName;
  }

  public void setSuperType( Type superType)
  {
    this.superType = superType;
  }

  public void addInterface( Type interface_)
  {
    interfaces.add( interface_);
  }

  public void setAccessFlags( AccessFlags flags)
  {
    this.flags = flags;
  }

  public void addMethod( MethodTypeInstance method)
  {
    methods.add( method);
  }

  public void addField( FieldInstance field)
  {
    fields.add( field);
  }

  public void setInner( boolean isInner)
  {
    this.isInner = isInner;
  }

  public void setIsLocal( boolean isLocal)
  {
    this.isLocal = isLocal;
  }

  public void setIsAnonymous( boolean isAnonymous)
  {
    this.isLocal = isLocal;
    this.isAnonymous = isAnonymous;
  }

  public void setContainingClass( ClassType containingClass)
  {
    this.containingClass = containingClass;
  }
  
  public void setInnerName( String innerName)
  {
    this.innerName = innerName;
  }

  public void addInnerClass( ClassType innerClass)
  {
    innerClasses.add( innerClass);
  }
}

