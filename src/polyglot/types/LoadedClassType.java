/*
 * LoadedClassType.java
 */

package jltools.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import jltools.util.TypedList;

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
  public LoadedClassType(Class theClass, TypeSystem typeSys) throws TypeCheckException {
    super( typeSys);    
    
    // Set up names and classType.    
    String rawName = theClass.getName(); // pkg1.pkg2.class$inner1$inner2
    this.packageName = TypeSystem.getPackageComponent(rawName);
    this.fullName = theClass.getName();
    this.shortName = TypeSystem.getShortNameComponent(fullName);

    // Set up the rest of the typing information
    this.flags = AccessFlags.flagsForInt(theClass.getModifiers());

    if (! this.fullName.equals("java.lang.Object")) {
      this.superType = ts.typeForClass(theClass.getSuperclass());
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
      this.containingClass = ts.typeForClass(outer);
      this.innerName = rawName;
    }
	
    // Now for the members!
    Field[]       fieldAry  = theClass.getDeclaredFields();
    Method[]      methodAry = theClass.getDeclaredMethods();
    Constructor[] constrAry = theClass.getDeclaredConstructors();
    List fieldLst  = new ArrayList(fieldAry.length + 1);
    List methodLst = new ArrayList(methodAry.length + constrAry.length + 1);

    for (int idx = 0; idx < fieldAry.length; ++idx) {
      fieldLst.add(fieldInstanceForField(fieldAry[idx]));
    }
    for (int idx = 0; idx < methodAry.length; ++idx) {
      methodLst.add(methodTypeForMethod(methodAry[idx]));
    }
    for (int idx = 0; idx < constrAry.length; ++idx) {
      methodLst.add(methodTypeForConstructor(constrAry[idx]));
    }
    this.fields  = new TypedList(fieldLst,  FieldInstance.class, true);
    this.methods = new TypedList(methodLst, MethodType.class, true);    
  }

  protected FieldInstance fieldInstanceForField(Field f) throws TypeCheckException 
  {
    String fieldName = f.getName();
    Type type = ts.typeForClass(f.getType());
    AccessFlags flags = AccessFlags.flagsForInt(f.getModifiers());    
    
    return new FieldInstance(fieldName, type, this,  flags);
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

}

