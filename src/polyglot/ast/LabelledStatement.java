/*
 * LabelledStatement.java
 */

package jltools.ast;

/**
 * LabelledStatement
 * 
 * Overview: A mutable representation of a Java statement with a
 * label.  A labeld statement contains the statement being labelled
 * and a string label.
 */

public class LabelledStatement extends Statement {
  /**
   * Effects: Creates a new LabelledStatement with label <label> and
   * statement <statement>.
   */
  public LabelledStatement (String label, Statement statement) {
    this.label = label;
    this.statement = statement;
  }

  /**
   * Effects: Returns the label associated with this statement.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Effects: Sets the label for this statement to be <newLabel>.
   */
  public void setLabel(String newLabel) {
    label = newLabel;
  }

  /**
   * Effects: Returns the statement being labelled in this
   * LabelledStatement. 
   */
  public Statement getStatement() {
    return statement;
  }

  /**
   * Effects: Sets the statement being labelled in this
   * LabelledStatement to be <newStatement>.
   */
  public void setStatement(Statement newStatement) {
    statement = newStatement;
  }

  public Node accept(NodeVisitor v) {
    return v.visitLabelledStatement(this);
  }

  /**
   * Requires: v will not transform the statement into anything other
   *    than another statement.
   *
   * Effects: visits the substatement of this with <v>.
   */
  public void visitChildren(NodeVisitor v) {
    statement = (Statement) statement.accept(v);
  }

  public Node copy() {
    LabelledStatement ls = new LabelledStatement(label, statement);
    ls.copyAnnotationsFrom(this);
    return ls;
  }

  public Node deepCopy() {
    LabelledStatement ls =
      new LabelledStatement(label, (Statement) statement.deepCopy());
    ls.copyAnnotationsFrom(this);
    return ls;
  }

  private String label;
  private Statement statement;
}
    
