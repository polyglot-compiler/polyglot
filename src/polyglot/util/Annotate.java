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

  static final int POSITION               = UniqueID.newIntID();
  static final int CHECKED_TYPE           = UniqueID.newIntID();

  static final int THROWS_SET             = UniqueID.newIntID();
  // true if the node has all paths ending in function termination (either throws or return).
  static final int EXPECTED_TYPE          = UniqueID.newIntID();
  static final int COMPLETES_NORMALLY     = UniqueID.newIntID();
  static final int IS_REACHABLE           = UniqueID.newIntID();
  
  // True for (PolyJ) expressions that are children of an ExpressionStatement
  static final int IS_EXPR_STATEMENT      = UniqueID.newIntID(); 
  // True for (PolyJ) expressions that are the left-hand side of an assignment.
  // Set during translation.
  static final int IS_LVALUE		  = UniqueID.newIntID(); 

    

  /**
   * Notes that o appeared at position p of the source.
   **/
  public static void setPosition(AnnotatedObject o, Position p) {
    o.setAnnotation(POSITION, p);
  }

  /**
   * Gets the source file position for o.
   **/
  public static Position getPosition(AnnotatedObject o) {
    return (Position) o.getAnnotation(POSITION);
  }

  /**
   * Sets the checked type of an object.
   **/
  public static void setCheckedType(AnnotatedObject o, Type t) {
    o.setAnnotation(CHECKED_TYPE, t);
  }

  /**
   * Returns the checked type of an object -- null if not set.
   **/
  public static Type getCheckedType(AnnotatedObject o) {
    return (Type) o.getAnnotation(CHECKED_TYPE);
  }

  /**
   * Sets the checked type of an object.
   **/
  public static void setExpectedType(AnnotatedObject o, Type t) {
    o.setAnnotation(EXPECTED_TYPE, t);
  }

  /**
   * Returns the checked type of an object -- null if not set.
   **/
  public static Type getExpectedType(AnnotatedObject o) {
    return (Type) o.getAnnotation(EXPECTED_TYPE);
  }
  /*
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
  
  public static boolean completesNormally(AnnotatedObject o )
  {
    Boolean b = ((Boolean)o.getAnnotation(COMPLETES_NORMALLY));
    if (b == null) 
      return false;
    return b.booleanValue();
  }

  public static void setCompletesNormally( AnnotatedObject o, boolean b )
  {
    o.setAnnotation( COMPLETES_NORMALLY, new Boolean (b ) );
  }

  public static boolean isReachable(AnnotatedObject o)
  {
    Boolean b = ((Boolean)o.getAnnotation(IS_REACHABLE));
    if (b == null) 
      return false;
    return b.booleanValue();
  }

  public static void setReachable(AnnotatedObject o, boolean b)
  {
    o.setAnnotation( IS_REACHABLE, new Boolean (b ) );
  }
  */


  /**
   * Label an Expression as being part of an ExpressionStatement,
   * meaning the result of the expression is not used
   * (Only used by PolyJ)
   **/
  public static void setStatementExpr(AnnotatedObject o, boolean b) {
    o.setAnnotation(IS_EXPR_STATEMENT, new Boolean(b));
  }

  public static boolean isStatementExpr(AnnotatedObject o) {
    Boolean b = (Boolean) o.getAnnotation(IS_EXPR_STATEMENT);
    if (b == null) 
      return false;    
    return b.booleanValue();
  }

  /**
   * Label an Expression as being the left side of an assignment
   * (Only used by PolyJ)
   **/
  public static void setLValue(AnnotatedObject o, boolean b) {
    o.setAnnotation(IS_LVALUE, new Boolean(b));
  }

  public static boolean isLValue(AnnotatedObject o) {
    Boolean b = (Boolean) o.getAnnotation(IS_LVALUE);
    if (b == null) 
      return false;    
    return b.booleanValue();
  }

  // Never instantiate this class.
  private Annotate() {}
}


