/*
 * TypeNode.java
 */

package jltools.ast;

import jltools.types.Type;
import jltools.util.CodeWriter;
import jltools.types.Context;
/**
 * TypeNode
 *
 * Overview: An TypeNode represents the syntactic representation of a Type.
 *   We make this a separate node so that Visitors may treat types separately,
 *   and so that Field/Method accesses can treat targets uniformly.
 **/
public class TypeNode extends Node {

  public TypeNode(Type type) {
    this.type = type;
  }

  public TypeNode(TypeNode other) {
    this.type = other.type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }

   void visitChildren(NodeVisitor vis)
   {
      // nothing to do
   }

   public Node typeCheck(Context c)
   {
      // FIXME: implement
      return this;
   }

   public void  translate(Context c, CodeWriter w)
  {
     w.write(type.getTypeString());
   }

   public void dump(Context c, CodeWriter w)
   {
     w.write(" ( TYPE: " + type.getTypeString() + ")");
   }
 

  public Node copy() {
    TypeNode tn = new TypeNode(type);
    tn.copyAnnotationsFrom(this);
    return tn;
  }

  public Node deepCopy() {
    return copy();
  }

  private Type type;
}

