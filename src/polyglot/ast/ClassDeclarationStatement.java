/*
 * ClassDeclarationStatement.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.Context;

/**
 * ClassDeclarationStatement
 *
 * Overeview: A ClassDeclarationStatement is a mutable representation
 * of the declaration of a class.  It consists of a ClassNode
 * representing the delcared class.
 */

public class ClassDeclarationStatement extends Statement {

  /**
   * Effects: Creates a new ClassDeclarationStatement for the class
   * defined in ClassNode.
   */
  public ClassDeclarationStatement(ClassNode classNode) {
    this.classNode = classNode;
  }

  /**
   * Effects: Returns the ClassNode declared by this.
   */
  public ClassNode getClassNode() {
    return classNode;
  }

  /**
   * Effects: Setts the ClassNode being declared by this to
   * <newClassNode>.
   */
  public void setClassNode(ClassNode newClassNode) {
    classNode = newClassNode;
  }
  public void translate ( Context c, CodeWriter w)
  {
    classNode.translate(c, w);
  }
  
  public void dump (Context c, CodeWriter w)
  {
    w.write (" CLASS DECLARATION");
    w.beginBlock();
    classNode.dump(c, w);
    w.endBlock();
    w.write ( " )");;
  }

  public Node typeCheck(Context c)
  {
    // FIXME: implement;
    return this;
  }


  /**
   * Requires: v will not transform the ClassNode into anything other
   * than another ClassNode.
   *
   * Effects: visits each of the children of this with <v>.  
   */
  public void visitChildren(NodeVisitor v) {
    classNode = (ClassNode) classNode.visit(v);
  }

  public Node copy() {
    return new ClassDeclarationStatement(classNode);
  }

  public Node deepCopy() {
    return new ClassDeclarationStatement((ClassNode)classNode.deepCopy());
  }

  private ClassNode classNode;
}
