package jltools.ast;

import jltools.types.*;
import jltools.util.*;

import java.util.*;


/**
 * A <code>Field</code> is an immutable representation of a Java field
 * access.  It consists of field name and may also have either a 
 * <code>Type</code> or an <code>Expression</code> containing the field being 
 * accessed.
 */
public class FieldExpression extends Expression 
{
  // FIXME
  private FieldInstance fi;

  protected final Node target;
  protected final String name;

  /**
   * Creates a new <code>EFieldExpression</code>.
   *
   * @pre <code>target</code> is either a <code>TypeNode</code> or an 
   * <code>Expression</code>.
   */
  public FieldExpression( Node ext, Node target, String name) 
  {
    if (target != null && ! (target instanceof TypeNode ||
			     target instanceof Expression))
     throw new InternalCompilerError( "Target of a field access must be a "
                                      + "type or expression.");
    this.ext = ext;
    this.target = target;
    this.name = name;
  }

    public FieldExpression( Node target, String name) {
	this(null, target, name);
    }

  /**
   * Lazily reconstruct this node. 
   */
  public FieldExpression reconstruct( Node ext, Node target, String name) 
  {
    if( this.target == target && this.ext == ext && this.name.equals( name)) {
      return this;
    }
    else {
      FieldExpression n = new FieldExpression( ext, target, name);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public FieldExpression reconstruct( Node target, String name) {
	return reconstruct(this.ext, target, name);
    }

  /**
   * Returns the target that the field is being accessed from.
   */
  public Node getTarget() 
  {
    return target;
  }

  /**
   * Returns the name of the field being accessed in the target of this node.
   */
  public String getName() 
  {
    return name;
  }

  // FIXME
  public FieldInstance getFieldInstance()
  {
    return fi;
  }

  /**
   * Visit the children of this node.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v),Node.condVisit(target, v), name);
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type ltype;

    if (target == null)
      ltype = null;
    else if( target instanceof Expression) {
      ltype = ((Expression)target).getCheckedType();
    }
    else if( target instanceof TypeNode) {
      ltype = ((TypeNode)target).getCheckedType();
    }
    else {
      throw new InternalCompilerError(
                              "Attempting field access on node of type " 
                              + target.getClass().getName());
    }

    if( ltype == null ||
        ltype.isClassType() ||
        ltype.isArrayType()) {
      if (name.equals("class"))
      {
        Annotate.setExpectedType( target, ltype);
        setCheckedType( c.getTypeSystem().getClass_());
      }      
      else
      {
        fi = c.getField( ltype, name);
        
        if (target != null)
          Annotate.setExpectedType( target, fi.getEnclosingType());
        setCheckedType( fi.getType());
      }
    }
    else {
      throw new SemanticException( 
                    "Cannot access a field of an expression of type "
                    + ltype.getTypeString());
    }

    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    if (target != null) 
    {
      if( target instanceof Expression) {
        translateExpression( (Expression)target, c, w);
        w.write( ".");
      }
      else if( target instanceof TypeNode) {
        if( ((TypeNode)target).getCheckedType() != c.getCurrentClass() ||
            name.equals( "class")) {
          target.translate(c, w);
          w.write( ".");
        }
      }
    }   
    w.write( name);
  }

  public void dump( CodeWriter w)
  {
    w.write( "FIELD ACCESS");
    w.write( " < " + name + " > ");
    dumpNodeInfo( w);
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
  
  public String toString() {
	  return name; //TO FIX
  }
}
    
  
