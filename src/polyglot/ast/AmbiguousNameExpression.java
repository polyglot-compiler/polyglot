/*
 * AmbiguousNameExpression.java
 */

package jltools.ast;

import jltools.types.*;
import jltools.util.*;

import java.util.*;


/**
 * AmbiguousNameExpression
 *
 * Overview: An AmbiguousNameExpression represents an ambiguous
 *    expression composed of a series of period-separated identifiers.
 *
 * Notes: In standard Java, an AmbiguousNameExpression will be one of:
 *     -- field-name{.nonstatic-field-name}*
 *     -- class-name.static-field-name{.nonstatic-field-name}*....
 *
 * Since we can identify locals at parse-time, we make it invariant
 * that the first component of an AmbiguousNameExpression is _not_ a local.
 *
 * In order to resolve the ambiguity, the spec requires that we inspect the
 * first identifier to determine whether it's a field.  If not, we look for
 * the longest possible prefix that's a class name.
 **/
public class AmbiguousNameExpression extends AmbiguousExpression {
  /**
   * Checks: lst has at least one element, and every element of lst is a
   *   String which contains no periods.
   * Effects: creates a new AmbiguousNameExpression for the names in lst.
   **/
  public AmbiguousNameExpression(List lst) {
    names = TypedList.copyAndCheck(lst, String.class, false);
    if (lst.size() < 1) throw new Error();
  }

  /**
   * Requires: strng is not empty, and does not begin or end with a '.'
   * Effects: creates a new AmbiguousNameExpression for the identifier in
   *   <strng>
   */
  public AmbiguousNameExpression(String strng) {
    names = new TypedList(new ArrayList(4), String.class, false);
    Enumeration enum = new java.util.StringTokenizer(strng, ".");
    while (enum.hasMoreElements())
      names.add(enum.nextElement());
  }

  public void addIdentifier(String strng) {
    names.add(strng);
  }

  /**
   * Returns a mutable TypedList of the identifiers in this
   * AmbiguousExpression.
   **/
  public TypedList getIdentifiers() {
    return names;
  }

  public String getName() {
    StringBuffer sb = new StringBuffer();
    Iterator iter = names.iterator();
    while(iter.hasNext())
    {
      sb.append((String)iter.next());
      if(iter.hasNext())
        sb.append('.');
    }
    return sb.toString();
  }

  public Node copy() {
    AmbiguousNameExpression ane = new AmbiguousNameExpression(names);
    ane.copyAnnotationsFrom(this);
    return ane;
  }

  public Node deepCopy() {
    return copy();
  }
  
  Object visitChildren(NodeVisitor vis) 
  { 
    return Annotate.getVisitorInfo( this);
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    /*
    for (Iterator i = names.listIterator(); i.hasNext(); )
    {
      w.write ((String)i.next());
      if(i.hasNext())
        w.write(".");
    }
    */
    
    throw new InternalCompilerError( 
			    "Attempted to translate an ambiguous node.");
  }

  public Node dump( CodeWriter w)
  {
    w.write ("( AMBIGOUS NAME < ");
    for (Iterator i = names.listIterator(); i.hasNext(); )
    {
      w.write( (String)i.next());
      if( i.hasNext()) {
        w.write( ".");
      }
    }
    w.write( " > ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node removeAmbiguities( LocalContext c) throws TypeCheckException
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
 	    top = new FieldExpression( new TypeNode( c.getCurrentClass()), 
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
      catch( TypeCheckException tce) 
      {
        if( top == null) {
          /* If it's not a local or field, then try and find a type. */
          try {
            last = (ClassType)c.getType( name);
            top = new TypeNode( last, name);
            
            /* Clear the name. */
            name = "";
          }
          catch( TypeCheckException tce2)
          {
            /* Not a local, field or type. Must be imcomplete. */
            name += ".";
          }
        }
        else throw tce;
      }
      
      if( top != null) {
        Annotate.setLineNumber( top, Annotate.getLineNumber( this));
      }
    }

    if( top == null) {
      throw new TypeCheckException( "No field or variable with name \"" + 
                                    name + "\".");
    }

    return top;
  }

  TypedList names;
}
