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
  static final int THROWS_SET             = 4;
  // true if the node has all paths ending in function termination (either throws or return).
  static final int TERMINATE_ALL_PATHS    = 5;

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

  public static void addThrows( AnnotatedObject o, Type t)
  {
    SubtypeSet s = (SubtypeSet)o.getAnnotation(THROWS_SET);
    if ( s == null)
    {
      s = new SubtypeSet();
      o.setAnnotation( THROWS_SET, s);
    }
    s.add ( t );
  }

  public static void addThrows( AnnotatedObject o, Collection c)
  {
    SubtypeSet s = (SubtypeSet)o.getAnnotation(THROWS_SET);
    if ( s == null)
    {
      s = new SubtypeSet();
      o.setAnnotation( THROWS_SET, s);
    }
    s.addAll ( c );
  }

  public static SubtypeSet getThrows(AnnotatedObject o) 
  {
    return (SubtypeSet) o.getAnnotation(THROWS_SET);
  }
  
  public static boolean terminatesOnAllPaths(AnnotatedObject o)
  {
    Boolean b = (Boolean) o.getAnnotation (TERMINATE_ALL_PATHS);
    if (b == null ) return false;
    return b.booleanValue();
  }

  public static void setTerminatesOnAllPaths ( AnnotatedObject o, boolean b)
  {
    o.setAnnotation ( TERMINATE_ALL_PATHS, new Boolean ( b ) );
  }
  

  // Never instantiate this class.
  private Annotate() {}
}


