/*
 * Annotate.java
 */

package jltools.util;

import jltools.ast.Expression;
import jltools.types.Type;

/**
 * Annotate
 *
 * Overview:
 *     This class contains the constants and methods used to wrap accesses
 *     to AnnotatedObject's methods.
 **/
public class Annotate {

  static final int LINE_NUMBER            = 1;
  static final int TYPE                   = 2;

  /**
   * Notes that o appeared at line i of the source.
   **/
  public static void setLineNumber(AnnotatedObject o, int i) {
    o.setAnnotation(LINE_NUMBER, new Integer(i));
  }

  /**
   * Gets the line number for o. (-1 for none.
   **/
  public static int getLineNumber(AnnotatedObject o) {
    Integer i = (Integer) o.getAnnotation(LINE_NUMBER);
    if (i == null) 
      return -1;
    return i.intValue();
  }

  /**
   * Sets the type of an Expression.
   **/
  public static void setType(Expression exp, Type t) {
    exp.setAnnotation(TYPE, t);
  }

  /**
   * Returns the type of an Expression -- null if not set.
   **/
  public static Type getType(Expression exp) {
    return (Type) exp.getAnnotation(TYPE);
  }
  

  // Never instantiate this class.
  private Annotate() {}
}


