/*
 * ParsedJavaClass.java
 */

package jltools.types;

import java.util.*;

import jltools.util.TypedList;
import jltools.util.AnnotatedObject;


/**
 * ParsedJavaClass
 *
 * Overview: 
 * A ParsedJavaClass represents a information that has been parsed (but not
 * necessarily type checked) from a .java file.
 **/
public class ParsedJavaClass extends JavaClassImpl 
{

  public ParsedJavaClass( TypeSystem ts)
  {
    this.ts = ts;
    interfaces = new TypedList( new LinkedList(), ClassType.class, false);
    methods = new TypedList( new LinkedList(), MethodTypeInstance.class,
                             false);
    fields = new TypedList( new LinkedList(), FieldInstance.class, false);
    innerClasses = new TypedList( new LinkedList(), JavaClass.class, false);
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

  public void setClassType( ClassType classType)
  {
    this.classType = classType;
  }

  public void setSuperType( ClassType superType)
  {
    this.superType = superType;
  }

  public void addInterface( ClassType interface_)
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

  public void setIsAnonymous( boolean isAnonymous)
  {
    this.isAnonymous = isAnonymous;
  }

  public void setContainingClass( Type containingClass)
  {
    this.containingClass = containingClass;
  }
  
  public void setInnerName( String innerName)
  {
    this.innerName = innerName;
  }

  public void addInnerClass( JavaClass innerClass)
  {
    innerClasses.add( innerClass);
  }
}

