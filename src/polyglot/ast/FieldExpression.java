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
      if( target instanceof Expression) {
        translateExpression( (Expression)target, c, w);
        w.write( ".");
      }
      else if( target instanceof TypeNode) {
        if( ((TypeNode)target).getCheckedType() != c.getCurrentClass()) {
          target.translate(c, w);
          w.write( ".");
        }
      }
    }
   
    w.write( name);
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

  public FieldInstance getFieldInstance()
  {
    return fi;
  }

  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    Type ltype;

    if( target instanceof Expression) {
      ltype = ((Expression)target).getCheckedType();
    }
    else if( target instanceof TypeNode) {
      ltype = ((TypeNode)target).getCheckedType();
    }
    else {
	System.out.println( "!!!" + target);
	System.out.println( "!!!" + target.getClass().getName());
      throw new InternalCompilerError(
                              "Attempting field access on node of type " 
                              + target.getClass().getName());
    }

    if( ltype instanceof ArrayType && name.equals( "length")) {
      setCheckedType( c.getTypeSystem().getInt());
    }
    else if( ltype instanceof ClassType) {
      if (name.equals("class"))
      {
        setCheckedType( c.getTypeSystem().typeForClass( java.lang.Class.class));
        // FIXME what to do about expected type?
      }      
      else
      {
        fi = c.getField( (ClassType)ltype, name);
        
        // FIXME is this expected type correct?
        Annotate.setExpectedType( target, fi.getEnclosingType());
        setCheckedType( fi.getType());
      }
    }
    else {
      throw new TypeCheckException( 
                    "Cannot access a field of an expression of type"
                    + ltype.getTypeString());
    }

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

  // the field instance which we are accessing;
  private FieldInstance fi;
  private Node target;
  private String name;
}
    
  
