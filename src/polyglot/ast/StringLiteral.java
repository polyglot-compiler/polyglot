/*
 * StringLiteral.java
 */

package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/** 
 * StringLiteral
 * 
 * Overview: A StringLiteral represents a mutable instance of a String
 *   which corresponds to a literal string in Java code.
 */
public class StringLiteral extends Literal {
  /**
   * Creates a new StringLiteral with value string
   */ 
  public StringLiteral (String string) {
    this.string = string;
  }
  
  /**
   * Effects: returns the string value of this
   */ 
  public String getString() {
    return string;
  }

  /**
   * Effects: sets the string value of this to <newString>
   */
  public void setString(String newString) {
    string = newString;
  }

  Object visitChildren(NodeVisitor vis)
  {
    //nothing to do
    return Annotate.getVisitorInfo( this);
  }

   public Node typeCheck(LocalContext c) throws TypeCheckException
   {
     setCheckedType( c.getType( "java.lang.String"));
     return this;
   }

   public void translate(LocalContext c, CodeWriter w)
   {
      w.write("\"" + string + "\"");
   }

   public Node dump( CodeWriter w)
   {
      w.write( "( STRING LITERAL");
      w.write( " < " + string + " > ");
      dumpNodeInfo( w);
      w.write( ")");
      return null;
   }
  
  public Node copy() {
    StringLiteral sl = new StringLiteral(string);
    sl.copyAnnotationsFrom(this);
    return sl;
  }

  public Node deepCopy() {
    return copy();
  }

  private String string;
}
