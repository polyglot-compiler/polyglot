/*
 * FieldNode.java
 */

package jltools.ast;

import jltools.types.AccessFlags;

/**
 * FieldNode
 *
 * Overview: A FieldNode is a mutable representation of the
 * declaration of a field of a class.  It consists of a set of
 * AccessFlags and a VariableDeclarationStatement.
 */

public class FieldNode extends ClassMember {
  /**
   * Effects: Creates a new FieldNode declaring variables from
   * <declare> with modified by the flags in <accessFlags>.
   */
  public FieldNode (AccessFlags accessFlags,
		    VariableDeclarationStatement declare) {
    this.accessFlags = accessFlags;
    this.declare = declare;
  }

  /**
   * Effects: Returns the AccessFlags for these fields.
   */
  public AccessFlags getAccessFlags() {
    return accessFlags;
  }

  /**
   * Effects: Sets the access flags for these fields to be <newFlags>.
   */
  public void setAccessFlags(AccessFlags newFlags) {
    accessFlags = newFlags;
  }

  /**
   * Effects: Returns the VariableDeclarationStatement which specifies
   * the type, names, and initialization expressions for the fields
   * declared as part of this FieldNode.
   */
  public VariableDeclarationStatement getDeclare() {
    return declare;
  }

  /**
   * Effects: Sets the VariableDeclarationStatement for this FieldNode
   * to be <newDeclare>.
   */
  public void setDeclare(VariableDeclarationStatement newDeclare) {
    declare = newDeclare;
  }

  public Node accept (NodeVisitor v) {
    return v.visitFieldNode(this);
  }

  public void visitChildren(NodeVisitor v) {
    declare = (VariableDeclarationStatement) declare.accept(v);
  }

  public Node copy() {
    FieldNode fn = new FieldNode(accessFlags.copy(), declare);
    fn.copyAnnotationsFrom(this);
    return fn;
  }

  public Node deepCopy() {
    FieldNode fn =
      new FieldNode(accessFlags.copy(),
		    (VariableDeclarationStatement) declare.deepCopy());
    fn.copyAnnotationsFrom(this);
    return fn;
  }

  private AccessFlags accessFlags;
  private VariableDeclarationStatement declare;
}


