package jltools.ast;

import jltools.types.*;
import jltools.util.*;


/** 
 * A <code>LocalVariableExpression</code> corresponds to an immutable reference
 * to a local variable (not a field of a class) in an expression.
 */
public class LocalVariableExpression extends Expression 
{
  protected final String name;
  // FIXME is this necessary?
  //  private FieldInstance fi;
    
  /** 
   * Creates a new local variable reference.
   */
  public LocalVariableExpression( String name) 
  {
    this.name = name;
  }

  public LocalVariableExpression reconstruct( String name)
  {
    if( this.name.equals( name)) {
      return this;
    }
    else {
      LocalVariableExpression n = new LocalVariableExpression( name);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /** 
   * Returns the name of the variable referenced by this node.
   */
  public String getName() 
  {
    return name;
  }

  /*
   * FIXME is this necessary?
   *
  public FieldInstance getFieldInstance()
  {
    return fi;
  }
  */

  Node visitChildren( NodeVisitor v) 
  {
    /* Nothing to do. */
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    FieldInstance fi = c.getField( null, name);
    setCheckedType( fi.getType());

    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( name);
  }

  public void dump( CodeWriter w)
  {
    w.write( "( LOCAL");
    w.write( " < " + name + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}
