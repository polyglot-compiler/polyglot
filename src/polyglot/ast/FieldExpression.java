package jltools.ast;

import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

import java.util.*;

//*** EVIL, TEMPORARY HACK
/*
import jltools.ext.jif.extension.*;
*/


/**
 * A <code>Field</code> is an immutable representation of a Java field
 * access.  It consists of field name and may also have either a 
 * <code>Type</code> or an <code>Expression</code> containing the field being 
 * accessed.
 */
public class FieldExpression extends Expression 
{
  protected FieldInstance fi;
  protected final String name;
  protected final Node target;

  /**
   * Creates a new <code>EFieldExpression</code>.
   *
   * @pre <code>target</code> is either a <code>TypeNode</code> or an 
   * <code>Expression</code>.
   */

  protected FieldExpression( Node ext, Node target, String name, FieldInstance fi)
  {
    if (target != null && ! (target instanceof TypeNode ||
			     target instanceof Expression))
     throw new InternalCompilerError(this, "Target of a field access must be a "
                                      + "type or expression.");
    this.ext = ext;
    this.target = target;
    this.name = name;
    this.fi = fi;

    if (name == null) {
      throw new InternalCompilerError(this, "Field name cannot be null");
    }
  }

  public FieldExpression( Node ext, Node target, FieldInstance fi)
  {
    this(ext, target, fi.getName(), fi);
  }

  public FieldExpression( Node ext, Node target, String name)
  {
    this(ext, target, name, null);
  }

  public FieldExpression( Node target, FieldInstance fi) {
    this(null, target, fi.getName(), fi);
  }

  public FieldExpression( Node target, String name) {
    this(null, target, name, null);
  }

  public Node exceptionCheck(ExceptionChecker ec)
    throws SemanticException
  {
    TypeSystem ts = ec.getTypeSystem();

    if (target instanceof Expression &&
	! (target instanceof SpecialExpression)) {
	ec.throwsException((ClassType) ts.getNullPointerException());
    }

    return this;
  }

  /**
   * Lazily reconstruct this node. 
   */
  public FieldExpression reconstruct( Node ext, Node target, String name, FieldInstance fi) 
  {
    if( this.target == target && this.ext == ext && this.name.equals( name) ) {
      if (this.fi == null) {
	if (fi == null) {
	  return this;
	}
      }
      else {
	if (this.fi.equals(fi)) {
	  return this;
	}
      }
    }

    FieldExpression n = new FieldExpression( ext, target, name, fi);
    n.copyAnnotationsFrom( this);
    return n;
  }

    public FieldExpression reconstruct( Node ext, Node target, String name) 
    {
      return reconstruct(ext, target, name, fi);
    }

    public FieldExpression reconstruct( Node target, String name) {
	return reconstruct(this.ext, target, name, null);
    }

    public FieldExpression reconstruct( Node target, FieldInstance fi) {
	return reconstruct(this.ext, target, fi.getName(), fi);
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

  public FieldInstance getFieldInstance()
  {
    return fi;
  }

  /**
   * Visit the children of this node.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v),Node.condVisit(target, v), name, fi);
  }

    public Node addThis() {
	return reconstruct(this.ext, target, name, fi);
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
      throw new InternalCompilerError(this,
                              "Attempting field access on node of type " 
                              + target.getClass().getName());
    }

    if( ltype == null ||
        ltype.isReferenceType()) {
      if (name.equals("class"))
      {
        Annotate.setExpectedType( target, ltype);
        setCheckedType( c.getTypeSystem().getClass_());
      }      
      else
      {
        if (fi == null) {
	  fi = c.getTypeSystem().getField(ltype, name, c);
	}
	else {
	  FieldInstance fi2 = c.getTypeSystem().getField(ltype, name, c);
	  if (! fi.equals(fi2)) {
	    throw new InternalCompilerError(this,
	      "Type checked field inconsistency: was " +
	      fi.getEnclosingType() + " now " + fi2.getEnclosingType());
	  }
	}
        
        if (target != null)
          Annotate.setExpectedType( target, fi.getEnclosingType());
        setCheckedType( fi.getType());
      }
    }
    else {
      throw new SemanticException( 
                    "Cannot access a field of an expression of type "
                    + ltype.getTypeString(),
		    Annotate.getPosition(this));
    }

    return this;
  }

  public String toString() {
    if (target != null) {
      return target + "." + name;
    }
    return name;
  }

  public void translate_no_override( LocalContext c, CodeWriter w)
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
}
    
  
