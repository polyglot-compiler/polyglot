/*
 * BranchStatement.java
 */

package jltools.ast;

/**
 * BranchStatement
 *
 * Overview: A BranchStatement is a mutable representation of a branch
 * statment in Java (a break or continue).  It consists of a type
 * corresponding to either break or continue and an optional label
 * specifing where to branch to.
 */
public class BranchStatement extends Statement {

  public static final int BREAK     = 0; // break statement
  public static final int CONTINUE  = 1; // continue statement


  public static final int MAX_TYPE = CONTINUE; // largest type used.

  /**
   * Requires: <type> be a valid type for a branch statement, as
   *    listed in the public static ints of this class. 
   *
   * Effects: Creates a new BranchStatement of type <type> which
   *    branches to the statement labelled by <label>.  If the
   *    statement does not specifiy a label to jump to, then <label>
   *    should be null.
   */
  public BranchStatement(int type, String label) {
    setType(type);
    this.label = label;
  }

  /**
   * Effects: Returns the type of this branch statement.
   */ 
  public int getType() {
    return type;
  }

  /**
   * Requires: A valid value for <newType> as listed in the public
   * static ints of this class.
   *
   * Effects: Sets the type of this BranchStatement to <newType>.
   */
  public void setType(int newType) {
    if (newType < 0 || newType > MAX_TYPE) {
      throw new IllegalArgumentException("Value for type of " +
					 "BranchStatement not valid.");
    }
    type = newType;
  }

  /**
   * Effects: Returns the label associated with this branch
   * statement.  If <this> is unlabled, returns null.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Effects: Sets the label associated with this BranchStatement to
   * be <newLabel>.  If <newLabel> is null, then this
   * BranchStatement becomes unlabbeled.
   */
  public void setLabel(String newLabel) {
    label = newLabel;
  }

  public Node accept(NodeVisitor v) {
    return v.visitBranchStatement(this);
  }

  public void visitChildren(NodeVisitor v) {
  }

  public Node copy() {
    BranchStatement bs = new BranchStatement(type, label);
    bs.copyAnnotationsFrom(this);
    return bs;
  }

  public Node deepCopy() {
    return copy();
  }
  private int type;
  private String label;
}

