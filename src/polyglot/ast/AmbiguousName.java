package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.SymbolReader;

import java.util.*;


/**
 * An <code>AmbiguousName</code> represents an ambiguous name 
 * composed of a series of period-separated identifiers.
 * <p>
 * The name may represent a <code>TypeNode</code> or an
 * <code>Expression</code>.
 **/
public class AmbiguousName extends Node implements AmbiguousNode {

  protected Node prefix;
  protected String name;

  public AmbiguousName( Node ext, Node prefix, String s) {
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
	throw new InternalCompilerError(this, "null ambiguous name");
      }
  }

  public AmbiguousName( Node prefix, String s) {
      this(null, prefix, s);
  }

  public AmbiguousName( String s) {
      this(null, null, s);
  }

  /**
   * Lazily reconstruct this node. If any of the dotted components in 
   * <code>s</code> differ from their respective components in 
   * <code>this.getName()</code> then return a new expression. Otherwise
   * return <code>this</code>.
   */
  public AmbiguousName reconstruct( Node ext, Node prefix, String name) {

    if (this.ext != ext || ! this.name.equals(name) || this.prefix != prefix) {
	AmbiguousName n = new AmbiguousName( ext, prefix, name);
	n.copyAnnotationsFrom( this);
	return n;
    }

    return this;
  }

  public AmbiguousName reconstruct( String s) {
      return reconstruct(this.ext, null, s);
  }

  public AmbiguousName reconstruct( Node prefix, String s) {
      return reconstruct(this.ext, prefix, s);
  }

  /**
   * Returns a new <code>AmbiguousName</code> whose expression
   * is equivalent to the current expression with <code>s</code> appended.
   *
   * @post Will copy annotations and extensions from <code>this</code> to the new node.
   */
  public AmbiguousName append( String s)
  {
    AmbiguousName n =  new AmbiguousName( this.ext, this, s );
    n.copyAnnotationsFrom( this);
    return n;
  }

  /**
   * Returns the prefix of this
   * <code>AmbiguousName</code>.
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
    else if (prefix instanceof AmbiguousNameExpression) {
      return ((AmbiguousNameExpression) prefix).getFullName() + "." + name;
    }
    else if (prefix instanceof TypeNode) {
      return ((TypeNode) prefix).getType().getTypeString() + "." + name;
    }
    else if (prefix != null) {
      return "<ambiguous>." + name;
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
      return reconstruct(Node.condVisit(ext, v), newPrefix, name);
  }

  public Node readSymbols( SymbolReader sr ) throws SemanticException
  {
    return null;
  } 

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    throw new InternalCompilerError(
                     "Attempt to type check an ambiguous node.");
  } 

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    throw new InternalCompilerError(
                     "Attempt to translate an ambiguous node.");
  } 

  public AmbiguousNameType toType(TypeSystem ts) {
    if (prefix != null) {
      if (prefix instanceof TypeNode) {
	return new AmbiguousNameType(ts, ((TypeNode) prefix).getType(), name);
      }
      else if (prefix instanceof AmbiguousName) {
	return new AmbiguousNameType(ts,
		((AmbiguousName) prefix).toType(ts), name);
      }
      else {
	throw new InternalCompilerError("AmbiguousName prefix is a " +
	    prefix.getClass().getName() + ", not a TypeNode or AmbiguousName");
      }
    }
    else {
      return new AmbiguousNameType(ts, null, name);
    }
  }

  public AmbiguousNameExpression toExpression() {
    AmbiguousNameExpression n = new AmbiguousNameExpression(ext, prefix, name);
    n.copyAnnotationsFrom( this);
    return n;
  }
  
  public Node removeAmbiguities( LocalContext c) throws SemanticException
  {
    Node top = null;

    if (prefix != null) {
	if (prefix instanceof TypeNode) {
	    /* Try static fields. */
	    Type type = ((TypeNode) prefix).getType();

	    if (type.isReferenceType()) {
		try {
		    ReferenceType refType = type.toReferenceType();
		    FieldInstance fi =
			c.getTypeSystem().getField(refType, name, c);
		    top = new FieldExpression(c.getExtensionFactory().
			getNewFieldExpressionExtension(),
			(TypeNode) prefix, fi );
		}
		catch (SemanticException e) {
		}
	    }

	    /* Try inner classes. */
	    if (top == null && (type.isClassType() || type.isPackageType())) {
		Type topType = c.getTypeSystem().checkAndResolveType(
		    new AmbiguousNameType(c.getTypeSystem(), name), type);
		top = new TypeNode(topType);
	    }
	}
	else if (prefix instanceof Expression) {
	    /* Try non-static fields. */
	    top = new FieldExpression(
		c.getExtensionFactory().getNewFieldExpressionExtension(),
		(Expression) prefix, name );
	}
    }
    else {
	try {
	    /* First try local variables and fields. */
	    VariableInstance vi = c.getVariable(name);

	    if (vi instanceof FieldInstance) {
		FieldInstance fi = (FieldInstance) vi;

		if (fi.getAccessFlags().isStatic() ) {
		    top = new FieldExpression(c.getExtensionFactory().
			getNewFieldExpressionExtension(),
			new TypeNode(fi.getEnclosingType()), fi );
		}
		else {
		    ClassType container = c.getFieldContainingClass(name);

		    TypeNode base = null;

		    if (!c.getTypeSystem().isSameType(container,
						      c.getCurrentClass())) {
			base = new TypeNode(container);
		    }

		    top = new FieldExpression(c.getExtensionFactory().
			getNewFieldExpressionExtension(),
			new SpecialExpression(c.getExtensionFactory().
			getNewSpecialExpressionExtension(),
			base, SpecialExpression.THIS),
			fi);
		}
	    }
	    else if (vi instanceof LocalInstance) {
	      top = new LocalVariableExpression(c.getExtensionFactory().
		getNewLocalVariableExpressionExtension(),
		name);
	    }
	    else {
	      throw new SemanticException("No field or variable with name \"" + 
		getFullName() + "\".", Annotate.getLineNumber(this));
	    }
	}
	catch (SemanticException e) {
	    /* Then try types. */
	    Type topType = c.getType(name);
	    if (topType != null) {
		top = new TypeNode(topType);
	    }
	}
    }

    if( top != null) {
      Annotate.setLineNumber( top, Annotate.getLineNumber( this));
    }
    else {
      throw new SemanticException( "No package, type, field, or variable " +
				   "with name \"" + getFullName() + "\".",
				   Annotate.getLineNumber(this));
    }

    return top;
  }

  /*
   * Possibly useful for debugging purposes, but usually not used.
   *
  public void translate_no_override( LocalContext c, CodeWriter w)
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
