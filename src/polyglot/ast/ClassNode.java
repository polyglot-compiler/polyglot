/*
 * ClassNode.java
 */

package jltools.ast;

import java.util.*;

import jltools.types.*;
import jltools.visit.SymbolReader;
import jltools.util.*;

/**
 * ClassNode
 *
 * Overeview: A ClassDeclarationStatement is a mutable representation
 * of the declaration of a class.  It consists of a class name, an
 * instance of AccessFlags, an optional super class, a list of
 * Interfaces, and a list of ClassMembers.
 */

public class ClassNode extends ClassMember {
  
  /**
   * Requires: <interfaces> contains only elements of type Type,
   * <classMemebers> contains only elements of type ClassMemeber.
   *
   * Effects: Creates a new instance of ClassNode for
   * a class named <name>, with access flags <accessFlags>, superclass
   * <superClass>, implementing interfaces <interfaceList>, and a
   * class body containing elements of <classMembers>.
   */
  public ClassNode(AccessFlags accessFlags,
		   String name,
		   TypeNode superClass,
		   List interfaceList,
		   List classMembers) {
    this.accessFlags = accessFlags;
    this.name = name;
    this.superClass = superClass;
    TypedList.check(interfaceList, TypeNode.class);
    this.interfaceList = new ArrayList(interfaceList);
    TypedList.check(classMembers, ClassMember.class);
    this.classMembers = new ArrayList(classMembers);
  }

  /**
   * Requires: <interfaces> contains only elements of type Type,
   * <classMemebers> contains only elements of type ClassMemeber.
   *
   * Effects: Creates a new instance of ClassNode for
   * a class named <name>, with access flags <accessFlags>, superclass
   * <superClass>, implementing interfaces <interfaceList>, and a
   * class body containing elements of <classMembers>.
   */
  public ClassNode(AccessFlags accessFlags,
		   String name,
		   Type superClass,
		   List interfaceList,
		   List classMembers) {
    this.accessFlags = accessFlags;
    this.name = name;
    this.superClass = new TypeNode(superClass);
    TypedList.check(interfaceList, Type.class);
    this.interfaceList = new ArrayList(interfaceList.size());
    for (Iterator i = interfaceList.iterator(); i.hasNext(); ) {
      this.interfaceList.add(i.next());
    }
    TypedList.check(classMembers, ClassMember.class);
    this.classMembers = new ArrayList(classMembers);
  }



  /**
   * Effects: returns the AccessFlags for this class declaration. 
   */
  public AccessFlags getAccessFlags() {
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
  public TypeNode getSuper() {
    return superClass;
  }

  /**
   * Effects: Sets the superclass of this to be <newSuper>.
   */
  public void setSuper(TypeNode newSuper) {
    superClass = newSuper;
  }

  /**
   * Effects: Sets the superclass of this to be <newSuper>.
   */
  public void setSuper(Type newSuper) {
    superClass = new TypeNode(newSuper);
  }

  /**
   * Effects: Returns a TypedListIterator which will return the
   * TypeNodes interfaces implemented by this ClassNode in
   * order.
   */
  public TypedListIterator interfaces() {
    return new TypedListIterator (interfaceList.listIterator(),
				  TypeNode.class,
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
  public void addInterface(TypeNode inter) {
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

  public void translate ( LocalContext c, CodeWriter w)
  {
    c.pushClass( type);
    w.write (accessFlags.getStringRepresentation() + "class " + name);
    if (superClass != null)
    {
      w.write (" extends ");
      superClass.translate(c, w);
    }
    if ( !interfaceList.isEmpty())
    {
      w.write (" implements " );
      for (Iterator i = interfaceList.listIterator(); i.hasNext() ; )
      {
        ((TypeNode)i.next()).translate(c, w);
        if ( i.hasNext())
             w.write (", ");
      }
    }
    w.newline(0);
    w.write ("{");
    w.beginBlock();
    for (Iterator i = classMembers.listIterator(); i.hasNext(); )
    {
      ((Node)i.next()).translate(c, w);
      if (i.hasNext())
      {
        w.newline(0);
        w.newline(0);
      }
    }
    w.endBlock();
    w.write( "}");
    w.newline(0);
    c.popClass();
  }
  
  public Node dump( CodeWriter w)
  {
    w.write( " ( CLASS < " + name + " >");
    w.write( " < " + accessFlags.getStringRepresentation() + "> ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node readSymbols( SymbolReader sr)
  {
    type = sr.pushClass( name);
    //    System.out.println( "reading: " + name);
    if( superClass != null) {
      type.setSuperType( superClass.getType());
    }
    type.setAccessFlags( accessFlags);

    visitChildren( sr);
    sr.popClass();
    
    return this;
  }

  public Node adjustScope( LocalContext c)
  {
    c.pushClass( type);
    return null;
  }

  public Node removeAmbiguities( LocalContext c) throws TypeCheckException
  {
    c.popClass(); 
    return this;
  }

  public Node typeCheck( LocalContext c)
  {
    // FIXME: implement;

    c.popClass(); 
    return this;
  }


  /**
   * Requires: v will not transform any ClassMember of the body of
   * this into anything other than another ClassMemeber.
   *
   * Effects: visits each of the children of this with <v>.  If <v>
   * returns null for a member of the class body, that element is
   * removed. 
   */
  Object visitChildren(NodeVisitor v) 
  {
    Object vinfo = Annotate.getVisitorInfo( this);

    if (superClass != null) {
      superClass = (TypeNode) superClass.visit(v);
      vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( superClass), vinfo);
    }
    for(ListIterator i = interfaceList.listIterator(); i.hasNext(); ) {
      TypeNode t = (TypeNode)i.next();
      vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( t), vinfo);
      i.set( t);
    }
    for(ListIterator i=classMembers.listIterator(); i.hasNext(); ) {
      ClassMember m = (ClassMember) i.next();
      m = (ClassMember) m.visit(v);
      if (m==null) {
	i.remove();
      }
      else {
        vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( m), vinfo);
	i.set(m);
      }
    }  
    return vinfo;
  }

  public Node copy() {
    ClassNode cn = new ClassNode(accessFlags.copy(),
				 name,
				 superClass,
				 new ArrayList(interfaceList),
				 classMembers);
    cn.copyAnnotationsFrom(this);
    return cn;
  }

  public Node deepCopy() {
    List newClassMembers = new ArrayList(classMembers.size());
    for (ListIterator i = classMembers.listIterator(); i.hasNext(); ) {
      ClassMember m = (ClassMember) i.next();
      newClassMembers.add(m.deepCopy());
    }
    ArrayList newInterfaceList = new ArrayList(interfaceList.size());
    for(Iterator i = interfaceList.iterator(); i.hasNext(); ) {
      newInterfaceList.add( ((TypeNode) i.next()).deepCopy() );
    }
    ClassNode cn = new ClassNode(accessFlags.copy(),
                        name,
		        (superClass == null ? null : new TypeNode(superClass)),
                        newInterfaceList,  newClassMembers);
    cn.copyAnnotationsFrom(this);
    return cn;
  }

  private AccessFlags accessFlags;
  private String name;
  private TypeNode superClass;
  private ParsedClassType type;
  private List interfaceList;
  private List classMembers;
}
