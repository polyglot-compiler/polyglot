package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * A <code>SpecialExpression</code> is an immutable representation of a
 * reference to <code>this</code> or <code>super</code in Java.  This
 * reference can be optionally qualified with a type such as 
 * <code>Foo.this</code>.
 */
public class SpecialExpression extends Expression 
{  
  public static final int SUPER = 0;
  public static final int THIS  = 1;
  
  // the highest kind used by this class
  protected static final int MAX_KIND = THIS;

  protected final TypeNode tn;
  protected final int kind;

  /**
   * Create a new <code>SpecialExpression</code>.
   *
   * @pre Requires that <code>kind</code> is a valid kind for this class.
   */
  public SpecialExpression( Node ext, TypeNode tn, int kind) 
  {
    if (kind < 0 || kind > MAX_KIND) {
      throw new IllegalArgumentException( "Invalid kind argument.");
    }
    this.ext = ext;
    this.tn = tn;
    this.kind = kind;
  }

    public SpecialExpression( TypeNode tn, int kind) {
	this(null, tn, kind);
    }

  /**
   * Lazily reconstruct this node.
   */
  public SpecialExpression reconstruct( Node ext, TypeNode tn, int kind)
  {
    if( this.tn == tn && this.kind == kind && this.ext == ext) {
      return this;
    }
    else {
      SpecialExpression n = new SpecialExpression( ext, tn, kind);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }
  
    public SpecialExpression reconstruct( TypeNode tn, int kind) {
	return reconstruct(this.ext, tn, kind);
    }

  /**
   * Returns the type of the qualifying type of this expression, or
   * <code>null</code> of there is no qualifying type.
   */
  public Type getType() 
  {
    return tn == null ? null : tn.getType();
  }

  /**
   * Returns the kind of this as specified by the public
   * <code>static ints</code> in this class.
   */
  public int getKind() 
  {
    return kind;
  }
  
  /**
   * Visit the children of this node.
   */
  public Node visitChildren( NodeVisitor v)
  {
      return reconstruct( Node.condVisit(this.ext, v), (TypeNode)Node.condVisit(tn, v), kind);
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    ClassType t;

    if (tn == null) {
      // Unqualified this expression
      t = c.getCurrentClass();
    }
    else {
      if (! tn.getType().isClassType()) {
	throw new SemanticException("Qualified " +
				    (kind == THIS ? "this" : "super") +
				    " expression must be of a class type",
				    Annotate.getLineNumber(tn));
      }

      t = (ClassType) tn.getType();
    }

    if ( kind == THIS) {
      setCheckedType(t);
    }
    else {
      setCheckedType(t.getSuperType());
    }

    return this;
  }

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    if( tn != null) {
      tn.translate( c, w);
      w.write( ".");
    }
    w.write( (kind == SUPER ? "super" : "this"));
  }

  public String toString() {
    return (tn != null ? (tn + ".") : "") + 
	   (kind == SUPER ? "super" : "this");
  }

  public void dump( CodeWriter w)
  {
    w.write( "SPECIAL");
    w.write( " < " + (kind == SUPER ? "super" : "this") + " > ");
    dumpNodeInfo( w);
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}
    
