/*
 * FieldExpression.java
 */

package jltools.ast;

import jltools.types.*;
import jltools.util.*;

import java.util.*;


/**
 * Overview: A Field is a mutable representation of a Java field
 * access.  It consists of field name and may also have either a Type
 * or an Expression containing the field being accessed.
 */
public class FieldExpression extends Expression {
  /**
   * Requires: <Target> is either a TypeNode or an Expression.
   *
   * Effects: Creates a new FieldExpression accessing a field named
   * <name> from optionally <target>.  
   */
  public FieldExpression(Node target, String name) {
    if (target != null && ! (target instanceof TypeNode ||
			     target instanceof Expression))
     throw new Error("Target of a field access must be a type or expression.");

    this.target = target;
    this.name = name;
  }

  /**
   * Effects: Returns the name of the field being accessed in this.
   */
  public String getName() {
    return name;
  }

  /**
   * Effects: Sets the name of the field being accessed by this to <newName>.
   */
  public void setName(String newName) {
    name = newName;
  }

  /**
   * Effects: Returns the target that the field is being accessed from.
   */
  public Node getTarget() {
    return target;
  }

  /**
   * Effects: Sets the target to be accessed to <newTarget>. 
   */
  public void setTarget(Node target) {
    if (target != null && ! (target instanceof TypeNode ||
			     target instanceof Expression))
     throw new Error("Target of a field access must be a type or expression.");

    this.target = target;
  }


  public void translate(LocalContext c, CodeWriter w)
  {
    if (target != null) 
    {
      target.translate(c, w);
      w.write("." + name);
    }
    else
    {
      w.write(name);
    }
  }

  public Node dump( CodeWriter w)
  {
    w.write( "( FIELD ACCESS");
    w.write( " < " + name + " > ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }

  public Node typeCheck( LocalContext c)
  {
    // FIXME; implement
    return this;
  }

  Object visitChildren(NodeVisitor v) 
  {
    Object vinfo = Annotate.getVisitorInfo( this);

    if (target != null) {
      target = target.visit(v);
      vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( target), vinfo);
    }

    return vinfo;
  }

  public Node copy() {
    FieldExpression fe = new FieldExpression(target, name);
    fe.copyAnnotationsFrom(this);
    return fe;
  }
	
  public Node deepCopy() {
    FieldExpression fe = new FieldExpression(target == null ? null :
					     target.deepCopy(), name);
    fe.copyAnnotationsFrom(this);
    return fe;
  }

  private Node target;
  private String name;
}
    
  
