/*
 * MethodExpression.java
 */

package jltools.ast;

import jltools.types.Type;
import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import jltools.types.LocalContext;
import jltools.util.CodeWriter;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 * Overview: A MethodExpression is a mutable representation of a Java
 * method call.  It consists of a method name and a list of arguments.
 * It may also have either a Type upon which the method is being
 * called or an expression upon which's value the method is being
 * called.
 */
public class MethodExpression extends Expression {
  /**
   * Requires: <target> is either a TypeNode or an Expression or null.
   *
   * Effects: Creates a new MethodExpression calling a method named
   * <name> with arguments in <arguments> being called optionally on
   * <target>.
   */
  public MethodExpression(Node target, String name,
			  List arguments) {
    if (target != null && ! (target instanceof TypeNode ||
			     target instanceof Expression))
      throw new Error("Target of a method call must be a type or expression.");

    this.target = target;
    this.name = name;
    TypedList.check(arguments, Expression.class);
    this.arguments = new ArrayList(arguments);
  }

  /**
   * Effects: Returns the name of the method being called in this.
   */
  public String getName() {
    return name;
  }

  /**
   * Effects: Sets the name of the method being called by this to <newName>.
   */
  public void setName(String newName) {
    name = newName;
  }

  /**
   * Effects: Adds <newArg> to the arguments of the method being
   * called by this.
   */
  public void addArgument(Expression newArg) {
    arguments.add(newArg);
  }

  /**
   * Effects: Returns the argument at position <pos> of the method
   * being called.  Throws IndexOutOfBoundsException if <pos> is not
   * valid.
   */
  public Expression getArgumentAt(int pos) {
    return (Expression) arguments.get(pos);
  }

  /**
   * Effects: Returns a TypedListIterator which will produce the
   * arugments of this MethodExpression in order.
   */
  public TypedListIterator arguments() {
    return new TypedListIterator(arguments.listIterator() ,
				 Expression.class,
				 false);
  }

  /**
   * Effects: Removes the argument at positions <pos>.  Throws
   * IndexOutOfBoundsException if <pos> is not valid.  */
  public void removeArgument(int pos) {
    arguments.remove(pos);
  }
  
  /**
   * Effects: Returns the target that that the method is being called on.
   * 
   * (The target will be either an Expression (possibly ambiguous) or a
   *  TypeNode.)
   */
  public Node getTarget() {
    return target;
  }

  /**
   * Requires: newTarget is an Expression or a TypeNode.
   *
   * Effects: Sets this method to be called on <newTarget>.
   */
  public void setTarget(Node newTarget) {
    if (newTarget != null && ! (newTarget instanceof TypeNode ||
				newTarget instanceof Expression))
      throw new Error("Target of a method call must be a type or expression.");
    
    target = newTarget;
  }
  
  public void translate(LocalContext c, CodeWriter w)
  {
    if (target != null)
    {
      target.translate(c, w);
      w.write ("." + name + "(");
    }
    else 
      w.write(name + "(");
    for(ListIterator it=arguments.listIterator(); it.hasNext(); ) {
      ((Expression)it.next()).translate(c, w);
      if (it.hasNext())
        w.write(", ");
    }
    w.write (")");
    
  }
  
  public void dump(LocalContext c, CodeWriter w)
  {
    w.write (" ( CALL " + name + ": ");
    dumpNodeInfo(c, w);
    
    if (target != null)
    {
      w.write (" ( TARGET: "  );
      target.dump(c, w);
      w.write(" ) ");
    }
    for(ListIterator it=arguments.listIterator(); it.hasNext(); ) 
    {
      w.write("(");
      ((Expression)it.next()).translate(c, w);
      w.write(")");
    }
    w.write(")");
  }

  public Node typeCheck(LocalContext c)
  {
    // fixme: implement
    return this;
  }
  
  public void visitChildren(NodeVisitor v) {
    if (target != null) {
      target = target.visit(v);
    }

    for(ListIterator it=arguments.listIterator(); it.hasNext(); ) {
      it.set(((Expression) it.next()).visit(v));
    }

  }

  public Node copy() {
    MethodExpression me = new MethodExpression(target,
					       name,
					       arguments);
    me.copyAnnotationsFrom(this);
    return me;
  }
	
  public Node deepCopy() {
    List newArguments = new ArrayList(arguments.size());
    for (ListIterator i=arguments.listIterator(); i.hasNext(); ) {
      newArguments.add(i.next());
    }
    MethodExpression me = new MethodExpression(target != null ? 
					          target.deepCopy() :
					          target,
					       name,
					       newArguments);
    me.copyAnnotationsFrom(this);
    return me;
  }

  private Node target;
  private String name;
  private List arguments;
}
    
  
