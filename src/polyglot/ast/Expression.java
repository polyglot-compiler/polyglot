package jltools.ast;

import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>Expression</code> represents any Java expression.  All expressions
 * must be subclasses of Expression.
 * <p>
 * This class also defines a number of precedence constants of the form
 * <code>PRECEDENCE_*</code>. These constants should be used at the return
 * value of methods which implement <code>getPrecedence()</code>.
 */
public abstract class Expression extends Node 
{
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
  /**
   * The precedence of literals, variables, field access, etc. 
   */
  public static final int PRECEDENCE_OTHER       = 0;

  /**
   * Implements the default behavior for expression. (That is, do nothing.) 
   */
  public Node readSymbols( SymbolReader sr)
  {
    return this;
  }

  /**
   * Return the precedence of the current expression. Lower values represent
   * tighter binding precendence.
   */
  public abstract int getPrecedence();

  /**
   * Correctly parenthesize the subexpression <code>expr<code> given
   * the its precendence and the precedence of the current expression.
   *
   * @param expr The subexpression.
   * @param c The context of translation.
   * @param w The output writer.
   */
  public final void translateExpression( Expression expr, 
                                         LocalContext c, 
                                         CodeWriter w)
  {
    if( expr.getPrecedence() > getPrecedence()) {
      w.write("(");
    }
    expr.translate_block(c, w);
    if( expr.getPrecedence() > getPrecedence()) {
      w.write( ")");
    }
  }

  /**
   * Sets the type of this expression to be <code>type</code>.  A 'null' value
   * signifies an unresolved type.
   */
  public void setCheckedType(Type type) {
    Annotate.setCheckedType(this, type);
  }

  /**
   * Gets the type of this expression.  A 'null' value signifies an unresolved 
   * type.
   */
  public Type getCheckedType() {
    return Annotate.getCheckedType(this);
  }    

  /**
   * Sets the expected type of this expression to be <code>type</code>.  
   */
  public void setExpectedType(Type type) {
    Annotate.setExpectedType(this, type);
  }

  /**
   * Gets the type of this expression.  A 'null' value signifies an unresolved 
   * type.
   */
  public Type getExpectedType() {
    return Annotate.getExpectedType(this);
  }  
}

