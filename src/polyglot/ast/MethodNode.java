/*
 * MethodNode.java
 */

package jltools.ast;

import jltools.types.Type;
import jltools.types.AccessFlags;
import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 * Overview: A MethodNode is a mutable representation of a methods
 * definition as part of a class body.  It consists of a method name,
 * a list of formal parameters, a list of exceptions which may be
 * thrown, a return type, access flags, and a method body.  A Method
 * may be a constructor in which case it does not have a name (as it
 * must be the same as the class, nor does it have a return type.
 */
public class MethodNode extends ClassMember {
  /**
   * Requires: all elements of <formals> are of type FormalParameter,
   * all elements of <exceptions> are of type Type.
   *
   * Overview: Creates a new MethodNode to represent a constructor
   * which takes <formals> as parameters, may throw exceptions in
   * <exceptions>, is modified by <accessFlags> and contains <body>
   * for a body.  
   */
  public MethodNode(AccessFlags accessFlags,
		    List formals,
		    List exceptions,
		    BlockStatement body) {
    this.accessFlags = accessFlags;
    TypedList.check(formals, FormalParameter.class);
    this.formals = new ArrayList(formals);
    TypedList.check(exceptions, Type.class);
    this.exceptions = new ArrayList(exceptions);
    this.body = body;
    this.name = null;
    this.returnType = null;
    this.constructor = true;
  }
  
  /**
   * Requires: all elements of <formals> are of type FormalParameter,
   * all elements of <exceptions> are of type Type.
   *
   * Overview: Creates a new MethodNode to represent a method by the
   * name <name> which takes <formals> as parameters, returns type
   * <returnType>, may throw exceptions in <exceptions>, is modified
   * by <accessFlags> and contains <body> for a body.
   */
  public MethodNode(AccessFlags accessFlags,
		    Type returnType,
		    String name,
		    List formals,
		    List exceptions,
		    BlockStatement body) {
    this.accessFlags = accessFlags;
    TypedList.check(formals, FormalParameter.class);
    this.formals = new ArrayList(formals);
    TypedList.check(exceptions, Type.class);
    this.exceptions = new ArrayList(exceptions);
    this.body = body;
    this.name = name;
    this.returnType = returnType;
    this.constructor = false;
  }

  /**
   * Effects: Returns true iff this MethodNode represents a constructor.
   */
  public boolean isConstructor() {
    return constructor;
  }

  /**
   * Effects: Returns the AccessFlags modifing this MethodNode.
   */
  public AccessFlags getAccessFlags() {
    return accessFlags;
  }

  /**
   * Effects: Sets the AccessFlags for this Method to be <newFlags>.
   */
  public void setAccssFlags(AccessFlags newFlags) {
    accessFlags = newFlags;
  }

  /**
   * Effects: Returns the return type of this MethodNode.
   */
  public Type getReturnType() {
    return returnType;
  }

  /**
   * Effects: Sets the return type for this MethodNode to be
   * <newReturnType>.
   */
  public void setReturnType(Type newReturnType) {
    returnType = newReturnType;
  }

  /**
   * Effects: Returns the name of this this method.
   */
  public String getName() {
    return name;
  }

  /**
   * Effects: Sets the name of the method reprented by this to <newName>.
   */
  public void setName(String newName) {
    name = newName;
  }
   
  /**
   * Effects: Adds a formal parameter <fp> to the list of arguments taken
   * by this method.
   */
  public void addFormalParameter(FormalParameter fp) {
    formals.add(fp);
  }

  /**
   * Effects: Returns the parameter of this method as position <pos>.
   * Throws IndexOutOfBoundsException if <pos> is not valid.
   */
  public FormalParameter getFormalParameter(int pos) {
    return (FormalParameter) formals.get(pos);
  }

  /**
   * Effects: Removes the formal parameter at position <pos>.  Throws
   * an IndexOutOfBounds if <pos> is not valid.
   */
  public void removeFormalParameter(int pos) {
    formals.remove(pos);
  }

  /**
   * Effects: Returns a TypedListIterator which produces the
   * FormalParameters in order of the method defined by this.
   */
  public TypedListIterator formalParameters() {
    return new TypedListIterator(formals.listIterator(),
				 FormalParameter.class,
				 false);
  }

  /**
   * Effects: Adds <excep> to the list of exceptions thrown by the
   * method defined by this.
   */
  public void addException(Type excep) {
    exceptions.add(excep);
  }

  /**
   * Effects: Returns the exception at position <pos> in the exception
   * list.  Throws IndexOutOfBoundsException if <pos> is not valid.
   */
  public Type getException(int pos) {
    return (Type) exceptions.get(pos);
  }

  /**
   * Effects: Removes the exception at position <pos> in the exception
   * list.  Throws an IndexOutOfBoundsException if <pos> is not valid.
   */
  public void removeException(int pos) {
    exceptions.remove(pos);
  }

  /**
   * Effects: Returns a typed list iterator which returns the
   * exceptions thrown by this in order.
   */
  public TypedListIterator exceptions() {
    return new TypedListIterator(exceptions.listIterator(),
				 Type.class,
				 false);
  }
  
  /**
   * Effects: Returns the BlockStatement representing the body of this
   * method.
   */
  public BlockStatement getBody() {
    return body;
  }

  /**
   * Effects: Sets the body of this to be <newBody>.
   */
  public void setBody(BlockStatement newBody) {
    body = newBody;
  }

  public Node accept(NodeVisitor v) {
    return v.visitMethodNode(this);
  }
  
  public void visitChildren(NodeVisitor v) {
    body = (BlockStatement) body.accept(v);
  }

  public Node copy() {
    MethodNode mn;
    if (isConstructor()) {
      mn =  new MethodNode(accessFlags.copy(),
			   formals,
			   exceptions,
			   body);
    }
    else {
      mn = new MethodNode(accessFlags,
			  returnType,
			  name,
			  formals,
			  exceptions,
			  body);
    }
    mn.copyAnnotationsFrom(this);
    return mn;
  }

  public Node deepCopy() {
    MethodNode mn;
    if (isConstructor()) {
      mn = new MethodNode(accessFlags.copy(),
			  formals,
			  exceptions,
			  (BlockStatement) body.deepCopy());
    }
    else {
      mn = new MethodNode(accessFlags,
			  returnType,
			  name,
			  formals,
			  exceptions,
			  (BlockStatement) body.deepCopy());
    }
    mn.copyAnnotationsFrom(this);
    return mn;
  }

  private boolean constructor;
  private AccessFlags accessFlags;
  private Type returnType;
  private String name;
  private List formals;
  private List exceptions;
  private BlockStatement body;
}
