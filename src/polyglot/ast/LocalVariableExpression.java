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
  private FieldInstance fi;
    
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

  public FieldInstance getFieldInstance()
  {
    return fi;
  }


  public Node visitChildren( NodeVisitor v) 
  {
    /* Nothing to do. */
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    /**
     * FIXME:
     * This was previously not here (ala spoons) since we weren't sure whether it
     * was necessary.  The switchStatement uses it to determine the labels
     * so that it can resolve final names to case lables. However, this brings up
     * the ugly point of whether we should reconstruct this or not. I am not 
     * reconstructing here, but perhaps we should?
     */
    fi = c.getField( null, name);
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
