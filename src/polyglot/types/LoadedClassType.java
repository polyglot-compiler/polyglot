/*
 * LoadedClassType.java
 */

package jltools.types;

import jltools.util.TypedList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;


/**
 * LoadedClassType
 *
 * Overview: 
 *    A LoadedClassType is a ClassType which derives from a file
 *    loaded at run time.
 **/
public class LoadedClassType extends ClassTypeImpl  {

  /**
   * Constructs a new LoadedClassType from a given class, within a given
   * typeSystem.
   **/
  public LoadedClassType(Class theClass, TypeSystem typeSys) 
       throws TypeCheckException 
  {
    super( typeSys);    
    this.theClass = theClass;
    
    // Set up names and classType.    
    String rawName = theClass.getName(); // pkg1.pkg2.class$inner1$inner2
    this.packageName = TypeSystem.getPackageComponent(rawName);
    this.fullName = theClass.getName();
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
  }

  public List getFields()
  {
    if( fields == null) {
      try {
        initializeFields();
      }
      catch( TypeCheckException e) {
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
      catch( TypeCheckException e) {
        e.printStackTrace();
        return new LinkedList();
      }
    }
    return super.getMethods();
  }

  protected void initializeFields() throws TypeCheckException
  {
    Field[]       fieldAry  = theClass.getDeclaredFields();
    List fieldLst  = new ArrayList(fieldAry.length + 1);

    for (int idx = 0; idx < fieldAry.length; ++idx) {
      fieldLst.add(fieldInstanceForField(fieldAry[idx]));
    }

    fields  = new TypedList(fieldLst,  FieldInstance.class, true);
  }

  protected void initializeMethods() throws TypeCheckException
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

  protected FieldInstance fieldInstanceForField(Field f) throws TypeCheckException 
  {
    String fieldName = f.getName();
    Type type = ts.typeForClass(f.getType());
    AccessFlags flags = AccessFlags.flagsForInt(f.getModifiers());    
    
    FieldInstance fi = new FieldInstance(fieldName, type, this,  flags);
    // gets the constant field and sets it.
    try
    {    
      fi.setConstantValue ( f.get(null) );
    }
    catch (Exception e) {}
    return fi;
  }

  protected MethodType methodTypeForMethod(Method m) throws TypeCheckException 
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

    return new MethodTypeInstance(ts, name, returnType, argList, excpList, flags);
  }

  protected MethodType methodTypeForConstructor(Constructor m) throws TypeCheckException
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

    return new ConstructorTypeInstance(ts, argList, excpList, flags);
  }

  Class theClass;
}

