package jltools.ast;

import jltools.util.*;
import jltools.types.LocalContext;

/**
 * A <code>BranchStatement</code> is an immutable representation of a branch
 * statment in Java (a break or continue).  It consists of a kind corresponding
 *  to either break or continue and an optional label specifing where to 
 * branch to.
 */
public class BranchStatement extends Statement {

  public static final int BREAK     = 0; // break statement
  public static final int CONTINUE  = 1; // continue statement

  protected static final int MAX_KIND = CONTINUE; // largest kind used.

  protected final int kind;
  protected final String label;

  /** 
   * Create a new <code>BranchStatement</code> without a label.
   *
   * @pre Requires that <code>kind</code> is a valid kind.
   * @param kind Indicates if this is a <code>break</code> or a
   *  <code>continue</code> statement.
   */
  public BranchStatement( Node ext, int kind) 
  {
    this( ext, kind, null);
  }

    public BranchStatement( int kind) {
	this( null, kind, null);
    }

  /**
   * Creates a new <code>BranchStatement</code> of kind <code>kind</code>
   * which branches to the statement labelled by <code>label</code>.
   *
   * @pre Requires that <code>kind</code> is a valid kind.
   * @param kind Indicates if this is a <code>break</code> or a 
   *  <code>continue</codee> statement.
   * @param label Specifies the label that this statement will branch to.
   */
  public BranchStatement( Node ext, int kind, String label) 
  {
    if (kind < 0 || kind > MAX_KIND) {
      throw new IllegalArgumentException("Value for kind of " +
					 "BranchStatement not valid.");
    }
    this.ext = ext;
    this.kind = kind;
    this.label = label;
  }

    public BranchStatement( int kind, String label) {
	this(null, kind, label);
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
   * @param kind Indicates if this is a <code>break</code> or a 
   *  <code>continue</codee> statement.
   * @param label Specifies the label that this statement will branch to.
   * @return A <code>BranchStatement</code> with the given kind and label.
   */
  public BranchStatement reconstruct( Node ext, int kind, String label) {
    if( this.kind == kind && this.ext == ext && ((this.label == null && label == null) 
                              || this.label.equals( label))) {
      return this;
    } 
    else {
      BranchStatement n = new BranchStatement( ext, kind, label);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  public BranchStatement reconstruct( int kind, String label) {
      return reconstruct(this.ext, kind, label);
  }

  /**
   * Returns the kind of this branch statement.
   */ 
  public int getKind() 
  {
    return kind;
  }

  /**
   * Returns the label associated with this branch statement.  If 
   * <code>this</code> is unlabled, returns <code>null</code>.
   */
  public String getLabel()
  {
    return label;
  }

  public Node visitChildren( NodeVisitor v)
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
    w.write( (kind == BREAK ? "break" : "continue") + 
               (label == null ? "; " : " " + label + "; "));
  }
  
  public void dump( CodeWriter w)
  {
    w.write( " ( BRANCH STATMENT < ");
    w.write( kind == BREAK ? "break > " : "continue > ");
    w.write( label == null ? "< " : label + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}

