/*
 * BlockStatement.java
 */

package jltools.ast;

import jltools.util.Assert;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;


/**
 * BlockStatement
 *
 * Overview: A BlockStatement represents a Java block statement -- a mutable
 *   sequence of statements.
 **/
public class BlockStatement extends Statement {
  /**
   * Effects: Create a new, empty BlockStatement.
   **/
  public BlockStatement() {
    statements = new ArrayList();
  }

  /**     
   * Add a new child <s> to this BlockStatement.
   **/
  public void addStatement(Statement s) {
    statements.add(s);
  }
  
  /**
   * Adds a new child <s> to this BlockStatement, such that the child
   * is at position <pos>.  Throws an IndexOutOfBoundsException if <pos>
   * is not a valid position.
   **/
  public void addStatement(Statement s, int pos) {
    statements.add(pos, s);
  }
 
  /**
   * Removes a child at position <pos> from this BlockStatement.
   * Throws an IndexOutOfBoundsException if <pos> is not a valid
   * position.
   **/
  public void removeStatement(int pos) {
    statements.remove(pos);
  }

  /**
   * Returns the child at position <pos>.  Throws an
   * IndexOutOfBoundsException if <pos> is not a valid position.
   **/
  public Statement statementAt(int pos) {
    return (Statement) statements.get(pos);
  }

  public Node accept(NodeVisitor v) {
    return v.visitBlockStatement(this);
  }

  /**
   * Requires: v will not transform a Statement into anything other than
   *    a Statement, an Expression, or Null.
   * Effects:
   *    Visits the children of this in order with <v>.  If <v> returns null,
   *    the statement is elided.  If it returns an Expression, it is wrapped
   *    as an ExpressionStatement.
   **/
  public void visitChildren(NodeVisitor v) {
    visitChildren(v, false);
  }

  /**
   * Requires: v will not transform a Statement into anything other than
   *    a Statement, an Expression, or Null.
   *
   * Effects:
   *    Visits the children of this in order with <v>.  If <v> returns
   *    null, the statement is elided.  If it returns an Expression, it is
   *    wrapped as an ExpressionStatement.
   *
   *    If <flatten> is true, all BlockStatements have their contents
   *    contents are interpolated into this statement.
   **/
  public void visitChildren(NodeVisitor v, boolean flatten) {
    for (ListIterator it = statements.listIterator(); it.hasNext(); ) {
      Node node = (Node) it.next();
      Node newNode = node.accept(v);
      if (newNode == null) {
	// Remove the node.
	it.remove();
	continue;
      } else if (flatten && newNode instanceof BlockStatement) {
	// Interpolate the subnodes.
	it.remove();
	BlockStatement bs = (BlockStatement) newNode;
	for (Iterator bsIt = bs.statements.iterator(); bsIt.hasNext(); ) {
	  it.add(bsIt.next());
	}
      } else if (node != newNode) {
	// The node changed.
	if (newNode instanceof Expression) {
	  it.set(new ExpressionStatement((Expression) newNode));
	} else {
	  Assert.assert(newNode instanceof Statement);  
	  it.set(newNode);
	}
      }
    }    
  }

  // RI: every member of statements is a Statement.
  private ArrayList statements;
}

