package jltools.ast;

import jltools.types.*;
import jltools.util.*;

import java.util.*;


/**
 * An <code>AmbiguousNameExpression</code> represents an ambiguous expression 
 * composed of a series of period-separated identifiers.
 * <p>
 * Notes: In standard Java, an <code>AmbiguousNameExpression</code> will be
 *  one of:
 * <ul>
 * <li> var-name{.nonstatic-field-name}*
 * <li> class-name.static-field-name{.nonstatic-field-name}*
 * <li> inner-class-name.static-field-name{.nonstatic-field-name}*
 * <li> class-name.inner-class-name.static-field-name{.nonstatic-field-name}*
 * </ul>
 * Where <i>var-name</i> is either a local variable or field of the current
 * a super class, or a containing class.
 * <p>
 * In order to resolve the ambiguity, the spec requires that we inspect the
 * first identifier to determine whether it's a field.  If not, we look for
 * the longest possible prefix that's a class name.
 **/
public class AmbiguousNameExpression extends AmbiguousExpression {

  protected Node prefix;
  protected String name;

  /**
   * Creates a new AmbiguousNameExpression for the identifier in
   * <code>s</code>.
   * @pre <code>s</code> is not empty, and does not begin or end with a '.'.
   */
  public AmbiguousNameExpression( Node ext, Node prefix, String s) {
      this.ext = ext;
      this.prefix = prefix;

      StringTokenizer st = new StringTokenizer(s, ".");

      while (st.hasMoreTokens()) {
	  String p = st.nextToken();

	  if (st.hasMoreTokens()) {
	    this.prefix = new AmbiguousName(ext, this.prefix, p);
	  }
	  else {
	    this.name = p;
	  }
      }

      if (this.name == null) {
	throw new InternalCompilerError("null ambiguous name");
      }
  }

  public AmbiguousNameExpression( Node prefix, String s) {
      this(null, prefix, s);
  }

  public AmbiguousNameExpression( String s) {
      this(null, null, s);
  }

  /**
   * Lazily reconstruct this node. If any of the dotted components in 
   * <code>s</code> differ from their respective components in 
   * <code>this.getName()</code> then return a new expression. Otherwise
   * return <code>this</code>.
   */
  public AmbiguousNameExpression reconstruct( Node ext,
    Node prefix, String name) {

    if (this.ext != ext || ! this.name.equals(name) || this.prefix != prefix) {
	AmbiguousNameExpression n =
	  new AmbiguousNameExpression( ext, prefix, name);
	n.copyAnnotationsFrom( this);
	return n;
    }

    return this;
  }

  public AmbiguousNameExpression reconstruct( String s) {
      return reconstruct(this.ext, null, s);
  }

  public AmbiguousNameExpression reconstruct( Node prefix, String s) {
      return reconstruct(this.ext, prefix, s);
  }

  /**
   * Returns a new <code>AmbiguousNameExpression</code> whose expression
   * is equivalent to the current expression with <code>s</code> appended.
   *
   * @post Will copy annotations and extensions from <code>this</code> to the new node.
   */
  public AmbiguousNameExpression append( String s)
  {
    AmbiguousName newPrefix = new AmbiguousName( this.ext, prefix, name );
    AmbiguousNameExpression n =  new AmbiguousNameExpression( this.ext,
	newPrefix, s );
    n.copyAnnotationsFrom( this);
    return n;
  }

  /**
   * Returns the prefix of this
   * <code>AmbiguousNameExpression</code>.
   */
  public Node getPrefix() {
    return prefix;
  }

  /**
   * Returns the entire expression as a dotted string.
   */
  public String getFullName() {
    if (prefix instanceof AmbiguousName) {
      return ((AmbiguousName) prefix).getFullName() + "." + name;
    }
    else if (prefix != null) {
      throw new InternalCompilerError("Cannot get full name of partially unambiguous AmbiguousNameExpression prefix isa " + prefix.getClass().getName() );
    }
    else {
      return name;
    }
  }

  public String getName() {
    return name;
  }

  public Node visitChildren( NodeVisitor v) 
  { 
      Node newPrefix = null;
      if (prefix != null) {
	newPrefix = prefix.visit(v);
      }
      return reconstruct(Node.condVisit(ext, v), newPrefix, getName());
  }

  public Node removeAmbiguities( LocalContext c) throws SemanticException
  {
    Node top = null;

    if (prefix != null) {
	if (prefix instanceof TypeNode) {
	    /* Try static fields. */
	    Type type = ((TypeNode) prefix).getType();

	    if (type.isReferenceType()) {
		ReferenceType refType = type.toReferenceType();
		FieldInstance fi =
		    c.getTypeSystem().getField(refType, name, c);
		top = new FieldExpression(
		    c.getTypeSystem().getNewFieldExpressionExtension(),
		    (TypeNode) prefix, fi );
	    }
	}
	else if (prefix instanceof Expression) {
	    /* Try non-static fields. */
	    top = new FieldExpression(
		c.getTypeSystem().getNewFieldExpressionExtension(),
		(Expression) prefix, name );
	}
    }
    else {
	/* First try local variables and fields. */
	VariableInstance vi = c.getVariable(name);

	if (vi instanceof FieldInstance) {
	    FieldInstance fi = (FieldInstance) vi;

	    if (fi.getAccessFlags().isStatic() ) {
		top = new FieldExpression(
		    c.getTypeSystem().getNewFieldExpressionExtension(),
		    new TypeNode(fi.getEnclosingType()), fi );
	    }
	    else {
		ClassType container = c.getFieldContainingClass(name);

		TypeNode base = null;

		if (container != c.getCurrentClass()) {
		    base = new TypeNode(container);
		}

		top = new FieldExpression(
		    c.getTypeSystem().getNewFieldExpressionExtension(),
		    new SpecialExpression(base, SpecialExpression.THIS),
		    fi);
	    }
	}
	else if (vi instanceof LocalInstance) {
	  top = new LocalVariableExpression(
	    c.getTypeSystem().getNewLocalVariableExpressionExtension(),
	    name);
	}
	else {
	  throw new SemanticException("No field or variable with name \"" + 
	    getFullName() + "\".", Annotate.getLineNumber(this));
	}
    }

    if( top != null) {
      Annotate.setLineNumber( top, Annotate.getLineNumber( this));
    }
    else {
      throw new SemanticException( "No field or variable " +
				    "with name \"" + getFullName() + "\".",
				    Annotate.getLineNumber(this) );
    }

    return top;
  }

  /*
   * Possibly useful for debugging purposes, but usually not used.
   *
  public void translate( LocalContext c, CodeWriter w)
  {
    for( Iterator i = names.listIterator(); i.hasNext(); ) {
      w.write( (String)i.next());
      if( i.hasNext()) {
        w.write( ".");
      }
    }
  }
  */

  public void dump( CodeWriter w)
  {
    w.write ("AMBIGUOUS NAME < ");
    w.write(getFullName());
    w.write( " > ");
    dumpNodeInfo( w);
  }
}
