/*
 * StringLiteral.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.Context;

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

   void visitChildren(NodeVisitor vis)
   {
      //nothing to do
   }

   public Node typeCheck(Context c)
   {
      // FIXME: implement
      return this;
   }

   public void  translate(Context c, CodeWriter w)
   {
      w.write("\"" + string + "\"");
   }

   public void dump(Context c, CodeWriter w)
   {
      w.write("( \"" + string + "\" )");
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
