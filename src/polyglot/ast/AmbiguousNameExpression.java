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
  
  public void visitChildren(NodeVisitor vis) { }

  public void translate( LocalContext c, CodeWriter w)
  {
    // FIXME: Shouldn't get in here.
    //w.write ("< AMBIGOUS NAME: ");
    //dump(c, w);
    //w.write ( "> ");
    
    for (Iterator i = names.listIterator(); i.hasNext(); )
    {
      w.write ((String)i.next());
      if(i.hasNext())
        w.write(".");
    }
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
    Expression e;
    TypeNode tn;
    FieldInstance fi;
    String name = getName();

    fi = c.getField( null, name);

    if( true /* c.isDefinedLocally( name) */) {
      e = new LocalVariableExpression( name);
    }
    else {
      tn = new TypeNode( fi.getEnclosingType());
      e = new FieldExpression( tn, name);
      Annotate.setLineNumber( tn, Annotate.getLineNumber( this));
    }

    Annotate.setLineNumber( e, Annotate.getLineNumber( this));
    e.setCheckedType( fi.getType());
    return e;
  }

  public Node typeCheck( LocalContext c)
  {
    // FIXME: implement;
    return this;
  }

  TypedList names;
}
