/*
 * LocalVariableExpression
 */

package jltools.ast;
import jltools.types.*;
import jltools.util.*;

/** 
 * LocalVariableExpression
 * 
 * Overview: A LocalVariableExpression corresponds to a mutalbe reference to
 *   a local variable (not a field of a class) in an expression.
 */

public class LocalVariableExpression extends Expression {
    
    /** 
     * Effects: Creates a new local variable reference to a
     * variable named <name>.
     */
    public LocalVariableExpression(String name) {
	this.name = name;
    }

    /** 
     * Effects: Returns the name of the variable referenced by this
     */
    public String getName() {
	return name;
    }

    /** 
     * Effects: Change the name of the variable referenced by this
     *    to <newName>.
     */
    public void setName(String newName) {
	name = newName;
    }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write(name);
  }

  public void dump( CodeWriter w)
  {
    w.write( "( LOCAL");
    w.write( " < " + name + " > ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node typeCheck(LocalContext c) throws TypeCheckException
  {
    fi = c.getField( null, name);
    setCheckedType( fi.getType());

    return this;
  }

  public FieldInstance getFieldInstance()
  {
    return fi;
  }

  Object visitChildren(NodeVisitor v) 
  {
    // nothing to do
    return Annotate.getVisitorInfo( this);
  }

    public Node copy() {
      LocalVariableExpression lve = new LocalVariableExpression(name);
      lve.copyAnnotationsFrom(this);
      return lve;
    }

    public Node deepCopy() {
      return copy();
    }

  private String name;
  private FieldInstance fi;
}
