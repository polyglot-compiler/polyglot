/*
 * ClassDeclarationStatement.java
 */

package jltools.ast;

import jltools.util.TypedListIterator;
import jltools.util.TypedList;
import jltools.types.Type;
import jltools.types.AccessFlags;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassDeclarationStatement
 *
 * Overeview: A ClassDeclarationStatement is a mutable representation
 * of the declaration of a class.  It consists of a class name, an
 * instance of AccessFlags, an optional super class, a list of
 * Interfaces, and a list of ClassMembers.
 */

public class ClassDeclarationStatement extends Statement {
  
  /**
   * Requires: <interfaces> contains only elements of type Type,
   * <classMemebers> contains only elements of type ClassMemeber.
   *
   * Effects: Creates a new instance of ClassDeclarationStatement for
   * a class named <name>, with access flags <accessFlags>, superclass
   * <superClass>, implementing interfaces <interfaceList>, and a
   * class body containing elements of <classMembers>.
   */
  public ClassDeclarationStatement(AccessFlags accessFlags,
				   String name,
				   Type superClass,
				   List interfaceList,
				   List classMembers) {
    this.accessFlags = accessFlags;
    this.name = name;
    this.superClass = superClass;
    TypedList.check(interfaceList, Type.class);
    this.interfaceList = new ArrayList(interfaceList);
    TypedList.check(classMembers, ClassMember.class);
    this.classMembers = new ArrayList(classMembers);
  }

  /**
   * Effects: returns the AccessFlags for this class declaration. 
   */
  public AccessFlags getAcccessFlags() {
    return accessFlags;
  }

  /**
   * Effects: sets the AccessFlags for this class declaration to <newFlags>.
   */
  public void setAccessFlags(AccessFlags newFlags) {
    accessFlags = newFlags;
  }

  /** 
   * Effects: Returns the name of this class.
   */
  public String getName() {
    return name;
  }

  /**
   * Effects: Sets the name of this class to be <newName>.
   */
  public void setName(String newName) {
    name = newName;
  }

  /**
   * Effects: Returns the type of the super class of this.
   */
  public Type getSuper() {
    return superClass;
  }

  /**
   * Effects: Sets the superclass of this to be <newSuper>.
   */
  public void setSuper(Type newSuper) {
    superClass = newSuper;
  }

  /**
   * Effects: Returns a TypedListIterator which will return the
   * interfaces implemented by this ClassDeclarationStatement in
   * order.
   */
  public TypedListIterator interfaces() {
    return new TypedListIterator (interfaceList.listIterator(),
				  Type.class,
				  false);
  }

  /**
   * Effects: Returns the interface at position <pos>.  Throws an
   * IndexOutOfBoundsException if <pos> is not valid.
   */
  public Type getInterfaceAt(int pos) {
    return (Type) interfaceList.get(pos);
  }

  /**
   * Effects: Adds <inter> to the list of interfaces implemented by
   * the class declared by this.
   */
  public void addInterface(Type inter) {
    interfaceList.add(inter);
  }

  /**
   * Effects: Removes the interface at <pos>.  Throws IndexOutOfBounds
   * if <pos> is not valid.
   */
  public void removeInterfaceAt(int pos) {
    interfaceList.remove(pos);
  }

  /**
   * Effects: Returns the element of the class body at <pos>.  Throws
   * IndexOutOfBoundsException if <pos> is not valid.  
   */
  public ClassMember getClassMemberAt(int pos) {
    return (ClassMember) classMembers.get(pos);
  }

  /**
   * Effects: Adds <member> to the class body of the class declared by this.
   */
  public void addClassMember(ClassMember member) {
    classMembers.add(member);
  }

  /**
   * Effects: Removes the class member at position <pos>.  If <pos> is
   * not valid an IndexOutOfBoundsException will be thrown.
   */
  public void removeClassMemberAt(int pos) {
    classMembers.remove(pos);
  }

  /**
   * Effects: Returns a TypedListIterator which yields each class
   * member of this class in order.  
   */
  public TypedListIterator classMembers() {
    return new TypedListIterator(classMembers.listIterator(),
				 ClassMember.class,
				 false);
  }

  public Node accept(NodeVisitor v) {
    return v.visitClassDeclarationStatement(this);
  }

  /**
   * Requires: v will not transform any ClassMember of the body of
   * this into anything other than another ClassMemeber.
   *
   * Effects: visits each of the children of this with <v>.  If <v>
   * returns null for a member of the class body, that element is
   * removed. 
   */
  public void visitChildren(NodeVisitor v) {
    for(ListIterator i=classMembers.listIterator(); i.hasNext(); ) {
      ClassMember m = (ClassMember) i.next();
      m = (ClassMember) m.accept(v);
      if (m==null) {
	i.remove();
      }
      else {
	i.set(v);
      }
    }
  }

  public Node copy() {
    ClassDeclarationStatement cds =
      new ClassDeclarationStatement(accessFlags.copy(),
				    name,
				    superClass,
				    interfaceList,
				    classMembers);
    cds.copyAnnotationsFrom(this);
    return cds;
  }

  public Node deepCopy() {
    List newClassMembers = new ArrayList(classMembers.size());
    for (ListIterator i = classMembers.listIterator(); i.hasNext(); ) {
      ClassMember m = (ClassMember) i.next();
      newClassMembers.add(m.deepCopy());
    }
    ClassDeclarationStatement cds =
      new ClassDeclarationStatement(accessFlags.copy(),
				    name,
				    superClass,
				    interfaceList,
				    newClassMembers);
    cds.copyAnnotationsFrom(this);
    return cds;
  }

  private AccessFlags accessFlags;
  private String name;
  private Type superClass;
  private List interfaceList;
  private List classMembers;
}
