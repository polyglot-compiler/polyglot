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

  protected final TypedList names;

  /**
   * Creates a new AmbiguousNameExpression for the identifier in
   * <code>s</code>.
   * @pre <code>s</code> is not empty, and does not begin or end with a '.'.
   */
  public AmbiguousNameExpression( Extension ext, String s) {
      names = new TypedList(new ArrayList(4), String.class, false);

      StringTokenizer st = new StringTokenizer( s, ".");

      while( st.hasMoreTokens()) {
	  names.add( st.nextToken());
      }
  }

    public AmbiguousNameExpression( String s) {
	this(null, s);
    }

  /**
   * Lazily reconstruct this node. If any of the dotted components in 
   * <code>s</code> differ from their respective components in 
   * <code>this.getName()</code> then return a new expression. Otherwise
   * return <code>this</code>.
   */
  public AmbiguousNameExpression reconstruct( String s) {
    StringTokenizer st = new StringTokenizer( s, ".");
    if( st.countTokens() != names.size()) {
      return new AmbiguousNameExpression( s);
    }
    else {
      for( Iterator iter = names.iterator(); iter.hasNext(); ) {
        if( !iter.next().equals( st.nextToken())) {
          AmbiguousNameExpression n = new AmbiguousNameExpression( s);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }

  /**
   * Returns a new <code>AmbiguousNameExpression</code> whose expression
   * is equivalent to the current expression with <code>s</code> appended.
   *
   * @post Will copy annotations from <code>this</code> to the new node.
   */
  public AmbiguousNameExpression append( String s)
  {
    AmbiguousNameExpression n = new AmbiguousNameExpression( getName() + "." 
                                                             + s);
    n.copyAnnotationsFrom( this);
    return n;
  }

  /**
   * Returns an immutable TypedList of the identifiers in this
   * <code>AmbiguousNameExpression</code>.
   */
  public TypedList getIdentifiers() {
    return names;
  }

  /**
   * Returns the entire expression as a dotted string.
   */
  public String getName() {
    StringBuffer sb = new StringBuffer();
    Iterator iter = names.iterator();

    while( iter.hasNext())
    {
      sb.append( (String)iter.next());
      if( iter.hasNext()) {
        sb.append( '.');
      }
    }

    return sb.toString();
  }

  public Node visitChildren( NodeVisitor v) 
  { 
    return this;
  }

  public Node removeAmbiguities( LocalContext c) throws SemanticException
  {
    Node top = null;
    String name = "";
    Type last = null;

    for (Iterator i = names.listIterator(); i.hasNext(); )
    {
      try {
        name += (String)i.next();

        /* First try local variables and fields. */
        FieldInstance fi;

        fi = c.getField( last, name );


        if( last == null && c.isDefinedLocally( name) ) {
          top = new LocalVariableExpression( name);
        }
        else {
          if( top == null) {
 	    top = new FieldExpression( null, //new TypeNode( c.getCurrentClass()), 
                                       fi.getName());
          }
          else {
            top = new FieldExpression( top, fi.getName());
          }
        }
        
        last = fi.getType();
        
        /* Clear the name. */
        name = "";
      }
      catch( SemanticException se) 
      {
        if( top == null) {
          /* If it's not a local or field, then try and find a type. */
          try {
            last = (ClassType)c.getType( name);
            top = new TypeNode( last, name);
            
            /* Clear the name. */
            name = "";
          }
          catch( SemanticException se2)
          {
            /* Not a local, field or type. Must be imcomplete. */
            name += ".";
          }
        }
        else throw se;
      }
      
      if( top != null) {
        Annotate.setLineNumber( top, Annotate.getLineNumber( this));
      }
    }

    if( top == null) {
      throw new SemanticException( "No field or variable with name \"" + 
                                    name + "\".");
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
    w.write ("( AMBIGOUS NAME < ");
    for( Iterator i = names.listIterator(); i.hasNext(); ) {
      w.write( (String)i.next());
      if( i.hasNext()) {
        w.write( ".");
      }
    }
    w.write( " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
