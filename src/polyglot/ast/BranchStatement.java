package jltools.ast;

import jltools.util.*;
import jltools.types.LocalContext;

/**
 * A <code>BranchStatement</code> is a immutable representation of a branch
 * statment in Java (a break or continue).  It consists of a type corresponding
 *  to either break or continue and an optional label specifing where to 
 * branch to.
 */
public class BranchStatement extends Statement {

  public static final int BREAK     = 0; // break statement
  public static final int CONTINUE  = 1; // continue statement

  public static final int MAX_TYPE = CONTINUE; // largest type used.

  protected final int type;
  protected final String label;

  /** 
   * Create a new <code>BranchStatement</code> without a label.
   *
   * @pre Requires that <code>type</code> is a valid type.
   * @param type Indicates if this is a <code>break</code> or a
   *  <code>continue</code> statement.
   */
  public BranchStatement( int type) 
  {
    this( type, null);
  }

  /**
   * Creates a new <code>BranchStatement</code> of type <code>type</code>
   * which branches to the statement labelled by <code>label</code>.
   *
   * @pre Requires that <code>type</code> is a valid type.
   * @param type Indicates if this is a <code>break</code> or a 
   *  <code>continue</codee> statement.
   * @param label Specifies the label that this statement will branch to.
   */
  public BranchStatement( int type, String label) 
  {
    if (type < 0 || type > MAX_TYPE) {
      throw new IllegalArgumentException("Value for type of " +
					 "BranchStatement not valid.");
    }

    this.type = type;
    this.label = label;
  }

  /**
   * Lazily reconstruct this node.
   * <p>
   * If the arguments are identical (using <code>==</code> and 
   * <code>equals</code>, respectively) to the fields of this object,
   * then return a reference to this object. Otherwise return a new
   * <code>BranchStatement</code>. If the desired statement has no label
   * then pass <code>null</code> for the second argument.
   *
   * @param type Indicates if this is a <code>break</code> or a 
   *  <code>continue</codee> statement.
   * @param label Specifies the label that this statement will branch to.
   * @return A <code>BranchStatement</code> with the given type and label.
   */
  public BranchStatement reconstruct( int type, String label) {
    if( this.type == type && ((this.label == null && label == null) 
                              || this.label.equals( label))) {
      return this;
    } 
    else {
      BranchStatement n = new BranchStatement( type, label);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the type of this branch statement.
   */ 
  public int getType() 
  {
    return type;
  }

  /**
   * Returns the label associated with this branch statement.  If 
   * <code>this</code> is unlabled, returns <code>null</code>.
   */
  public String getLabel()
  {
    return label;
  }

  Node visitChildren( NodeVisitor v)
  {
    /* Nothing to do. */
    return this;
  }

  public Node typeCheck(LocalContext c)
  {
    return this;
  }

  // FIXME write flowCheck

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( (type == BREAK ? "break" : "continue") + 
               (label == null ? "; " : " " + label + "; "));
  }
  
  public void dump( CodeWriter w)
  {
    w.write( " ( BRANCH STATMENT < ");
    w.write( type == BREAK ? "break > " : "continue > ");
    w.write( label == null ? "< " : label + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}

