/*
 * SpecialExpression.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.Context;
import jltools.types.Type;

/**
 * SpecialExpression
 * 
 * Overview: A SpecialExpression is a mutable representation of a
 * reference to "this" or "super" in Java.  This reference can be
 * optionally qualified with a type such as "foo.this".
 */

public class SpecialExpression extends Expression {
  
  public static final int SUPER = 0;
  public static final int THIS  = 1;
  
  // the highest type used by this class
  public static final int MAX_TYPE = THIS;


  /**
   * Requires: <kind> be one of the public static ints of this class.
   * 
   * Effects: Creates a new SpecialExpression of the given kind,
   * qualified by <type> if <type> is not null.
   */
  public SpecialExpression(TypeNode type, int kind) {
    setKind(kind);
    this.type = type;
  }

  public SpecialExpression(Type type, int kind) {
    this(new TypeNode(type), kind);
  }
  
  /**
   * Effects: Returns the TypeNode of the qualifying type of this, or
   * null of there is no qualifying type.
   */
  public TypeNode getType() {
    return type;
  }

  /**
   * Effects: Sets the TypeNode of the qualifying type of this to <newType>.
   */
  public void setType(TypeNode newType) {
    type = newType;
  }

  /**
   * Effects: Returns the kind of this as specified by the public
   * static ints in this class.
   */
  public int getKind() {
    return kind;
  }

  /**
   * Requires: <newKind> is a valid value as defined by the public
   * static ints of this class.
   *
   * Effects: Sets the kind of this to be <newKind>.
   */
  public void setKind(int newKind) {
    if (newKind < 0 || newKind > MAX_TYPE) {
      throw new IllegalArgumentException("kind argument not valid");
    }
    kind = newKind;
  }
  
   void visitChildren(NodeVisitor vis)
   {
      if (type != null) {
	 type = (TypeNode) type.visit(vis);
      }
   }

   public Node typeCheck(Context c)
   {
      // FIXME: implement
      return this;
   }

   public void translate(Context c, CodeWriter w)
   {
     if(type != null) {
       w.write(type.getType().getTypeString() + ".");
     }
     w.write((kind == SUPER ? "super" : "this"));
   }

   public void dump(Context c, CodeWriter w)
   {
     // FIXME
     w.write( " ( (" + type.getType().getTypeString() + " ) ( " + 
              (kind == SUPER ? "SUPER" : "THIS" ) + ") )");
     
   }

  public Node copy() {
    SpecialExpression se = new SpecialExpression (type, kind);
    se.copyAnnotationsFrom(this);
    return se;
  }

  public Node deepCopy() {
    SpecialExpression se =
      new SpecialExpression ((TypeNode) (type==null?null:type.deepCopy()),
			     kind);
    se.copyAnnotationsFrom(this);
    return se;
  }

  private TypeNode type;
  private int kind;
}
    
