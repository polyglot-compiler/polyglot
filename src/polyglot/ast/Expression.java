/*
 * Expression.java
 */

package jltools.ast;

import jltools.types.*;
import jltools.visit.SymbolReader;
import jltools.util.*;

/**
 * Expression
 *
 * Overview: An Expression represents any Java expression.  All expressions
 *    must be subclasses of Expression.
 **/
public abstract class Expression extends Node {

  public Node readSymbols( SymbolReader sr)
  {
    return this;
  }

  /**
   * Sets the type of this expression to be <type>.  A 'null' value signifies
   * an unresolved type.
   **/
  public void setCheckedType(Type type) {
    Annotate.setCheckedType(this, type);
  }

  /**
   * Gets the type of this expression.  A 'null' value signifies
   * an unresolved type.
   **/
  public Type getCheckedType() {
    return Annotate.getCheckedType(this);
  }    

   /**
   * Sets the expected type of this expression to be <type>.  
   **/
  public void setExpectedType(Type type) {
    Annotate.setExpectedType(this, type);
  }

  /**
   * Gets the type of this expression.  A 'null' value signifies
   * an unresolved type.
   **/
  public Type getExpectedType() {
    return Annotate.getExpectedType(this);
  }  

  public abstract int getPrecedence();

  public final void translateExpression( Expression expr, 
                                         LocalContext c, 
                                         CodeWriter w)
  {
    if( expr.getPrecedence() > getPrecedence()) {
      w.write( "(");
    }
    expr.translate( c, w);
    if( expr.getPrecedence() > getPrecedence()) {
      w.write( ")");
    }
  }


  public static final int PRECEDENCE_UNKNOWN     = Integer.MAX_VALUE;
  public static final int PRECEDENCE_UNARY       = 1;
  public static final int PRECEDENCE_CAST        = 1;
  public static final int PRECEDENCE_MULT        = 2;
  public static final int PRECEDENCE_ADD         = 3;
  public static final int PRECEDENCE_SHIFT       = 4;
  public static final int PRECEDENCE_INEQUAL     = 5;
  public static final int PRECEDENCE_INSTANCE    = 5;
  public static final int PRECEDENCE_EQUAL       = 6;
  public static final int PRECEDENCE_BAND        = 7;
  public static final int PRECEDENCE_BXOR        = 8;
  public static final int PRECEDENCE_BOR         = 9;
  public static final int PRECEDENCE_CAND        = 10;
  public static final int PRECEDENCE_COR         = 11;
  public static final int PRECEDENCE_TERN        = 12;
  public static final int PRECEDENCE_ASSIGN      = 13;
  public static final int PRECEDENCE_OTHER       = 0;
}

