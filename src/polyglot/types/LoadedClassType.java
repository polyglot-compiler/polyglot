package jltools.types;

import jltools.util.TypedList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import java.io.*;
import java.util.*;


/**
 * A <code>LoadedClassType</code> is a class type which is derived from a file
 * loaded at run time.
 * <p>
 * Note that though this class is <code>Serializable</code> it should never
 * actually be deserialized, since the default behavior is to only serialize
 * the name of the class as an <code>AmbiguousType</code>.
 */
public class LoadedClassType extends ClassTypeImpl  
{
  static final long serialVersionUID = 8605280214426739323L;

  protected Class theClass;

  /**
   * Constructs a new LoadedClassType from a given class, within a given
   * type sytem.
   */
  public LoadedClassType(TypeSystem ts, Class theClass) 
       throws SemanticException 
  {
    super( ts);    
    this.theClass = theClass;

    // Set up names and classType.    
    String rawName = theClass.getName(); // pkg1.pkg2.class$inner1$inner2
    this.packageName = TypeSystem.getPackageComponent(rawName);
    this.fullName = theClass.getName().replace( '$', '.');
    this.shortName = TypeSystem.getShortNameComponent(fullName);

    // Set up the rest of the typing information
    this.flags = AccessFlags.flagsForInt(theClass.getModifiers());

    if (! this.fullName.equals("java.lang.Object")) {
      Class superClass = theClass.getSuperclass();
      if( superClass != null) {
        this.superType = ts.typeForClass(theClass.getSuperclass());
      }
      else {
        this.superType = null;
      }
    }

    Class[] interfaceAry = theClass.getInterfaces();
    List interfaceLst = new ArrayList(interfaceAry.length +1);      
    for (int idx = 0; idx < interfaceAry.length; ++idx) {
      interfaceLst.add(ts.typeForClass(interfaceAry[idx]));
    }
    this.interfaces = new TypedList(interfaceLst, ClassType.class, true);

    // Set up the rest of the inner information.
    Class outer = theClass.getDeclaringClass();
    this.isInner = outer != null;
    if (this.isInner) {
      // FIXME: what about classes inside methods?
      this.isAnonymous = isInner && 
	Character.isDigit(this.shortName.charAt(0));    
      this.containingClass = (ClassType)ts.typeForClass(outer);
      this.innerName = rawName;
    }

    /* Now for the members, add these lazily. 
     * That is only add them if someone asks for them. */
    this.fields = null;
    this.methods = null;
    this.innerClasses = null;
  }

  public List getFields()
  {
    if( fields == null) {
      try {
        initializeFields();
      }
      catch( SemanticException e) {
        e.printStackTrace();
        return new LinkedList();
      }
    }
    return super.getFields();
  }

  public List getMethods()
  {
    if( methods == null) {
      try {
        initializeMethods();
      }
      catch( SemanticException e) {
        e.printStackTrace();
        return new LinkedList();
      }
    }
    return super.getMethods();
  }

  public List getInnerClasses()         
  { 
    if( innerClasses == null) {
      try {
        initializeInnerClasses();
      }
      catch( SemanticException e) {
        e.printStackTrace();
        return new LinkedList();
      }
    }
    return super.getInnerClasses();
  }

  public Type getInnerNamed(String name)
  {
    if( innerClasses == null) {
      try {
        initializeInnerClasses();
      }
      catch( SemanticException e) {
        e.printStackTrace();
        return null;
      }
    }
    return super.getInnerNamed( name);
  }

  protected void initializeFields() throws SemanticException
  {
    Field[]       fieldAry  = theClass.getDeclaredFields();
    List fieldLst  = new ArrayList(fieldAry.length + 1);

    for (int idx = 0; idx < fieldAry.length; ++idx) {
      fieldLst.add(fieldInstanceForField(fieldAry[idx]));
    }

    fields  = new TypedList(fieldLst,  FieldInstance.class, true);
  }

  protected void initializeMethods() throws SemanticException
  {
    Method[]      methodAry = theClass.getDeclaredMethods();
    Constructor[] constrAry = theClass.getDeclaredConstructors();
    List methodLst = new ArrayList(methodAry.length + constrAry.length + 1);

    for (int idx = 0; idx < methodAry.length; ++idx) {
      methodLst.add(methodTypeForMethod(methodAry[idx]));
    }
    for (int idx = 0; idx < constrAry.length; ++idx) {
      methodLst.add(methodTypeForConstructor(constrAry[idx]));
    }

    methods = new TypedList(methodLst, MethodType.class, true);    
  }

  protected void initializeInnerClasses() throws SemanticException
  {
    // do inner classes
    Class[] innerAry = theClass.getDeclaredClasses();
    List innerLst = new ArrayList(innerAry.length +1);      
    for (int idx = 0; idx < innerAry.length; ++idx) {
      innerLst.add(ts.typeForClass(innerAry[idx]));
    }
    this.innerClasses = new TypedList(innerLst, Type.class, true);

  }

  protected FieldInstance fieldInstanceForField(Field f) 
    throws SemanticException 
  {
    String fieldName = f.getName();
    Type type = ts.typeForClass(f.getType());
    AccessFlags flags = AccessFlags.flagsForInt(f.getModifiers());    
    
    FieldInstance fi = ts.newFieldInstance(fieldName, type, this,  flags);
    // gets the constant field and sets it.
    try
    {    
      fi.setConstantValue ( f.get(null) );
    }
    catch (Exception e) {}
    return fi;
  }

  protected MethodType methodTypeForMethod(Method m) throws SemanticException 
  {
    String name = m.getName();
    AccessFlags flags = AccessFlags.flagsForInt(m.getModifiers());
    Type returnType = ts.typeForClass(m.getReturnType());
    Class[] args = m.getParameterTypes();
    List argList = new ArrayList(args.length+1);
    for(int idx = 0; idx < args.length; idx++) {
      argList.add(ts.typeForClass(args[idx]));
    }
    Class[] excpns = m.getExceptionTypes();
    List excpList = new ArrayList(excpns.length+1);
    for(int idx = 0; idx < excpns.length; idx++) {
      excpList.add(ts.typeForClass(excpns[idx]));
    }

    return new MethodTypeInstance(ts, this, name, returnType, 
                                  argList, excpList, flags);
  }

  protected MethodType methodTypeForConstructor(Constructor m) 
    throws SemanticException
  {
    AccessFlags flags = AccessFlags.flagsForInt(m.getModifiers());
    Class[] args = m.getParameterTypes();
    List argList = new ArrayList(args.length+1);
    for(int idx = 0; idx < args.length; idx++) {
      argList.add(ts.typeForClass(args[idx]));
    }
    Class[] excpns = m.getExceptionTypes();
    List excpList = new ArrayList(excpns.length+1);
    for(int idx = 0; idx < excpns.length; idx++) {
      excpList.add(ts.typeForClass(excpns[idx]));
    }
    
    return new MethodTypeInstance(ts, this, null, null, argList, excpList, flags, true);
  }
}

