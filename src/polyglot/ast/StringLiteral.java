/*
 * StringLiteral.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.LocalContext;
import jltools.types.ClassType;

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

   public Node typeCheck(LocalContext c)
   {
      setCheckedType( new ClassType( c.getTypeSystem(), "java.lang.String", true));
      return this;
   }

   public void translate(LocalContext c, CodeWriter w)
   {
      w.write("\"" + string + "\"");
   }

   public void dump(LocalContext c, CodeWriter w)
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
