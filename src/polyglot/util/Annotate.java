/*
 * Annotate.java
 */

package jltools.util;

import jltools.types.Type;

import java.util.*;

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
  static final int ERROR                  = 3;

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
   * Sets the type of an object.
   **/
  public static void setType(AnnotatedObject o, Type t) {
    o.setAnnotation(TYPE, t);
  }

  /**
   * Returns the type of an object -- null if not set.
   **/
  public static Type getType(AnnotatedObject o) {
    return (Type) o.getAnnotation(TYPE);
  }

  public static void addError(AnnotatedObject o, ErrorInfo e)
  {
    List a = getErrors(o);
    if (a == null) {
      a = new LinkedList();
      a.add(e);
      o.setAnnotation(ERROR, o);
    }
    else {
      a.add(e);
    }
  }

  public static List getErrors(AnnotatedObject o)
  {
    return (List) o.getAnnotation(ERROR);
  }                           

  // Never instantiate this class.
  private Annotate() {}
}


