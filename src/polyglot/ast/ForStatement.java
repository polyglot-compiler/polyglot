/*
 * ForStatement.java
 */

package jltools.ast;

import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * ForStatement
 *
 * Overview: A mutable representation of a Java language For
 *   statment.  Contains a statement to be executed and an expression
 *   to be tested indicating whether to reexecute the statement.
 */ 
public class ForStatement extends Statement {
  /**
   * Checks: Every element of incrementors is an Expression.
   * Effects: Creates a new ForStatement with a body <statement>,
   *    initializer <initializer>, condition <condition>, and incrementors
   *    <incrementors>.
   */
  public ForStatement(Statement initializer,
		      Expression condition,
		      List incrementors,
		      Statement body) {
    TypedList.check(incrementors, Expression.class);
    this.initializer = initializer;
    this.condition = condition;
    this.body = body;
    this.incrementors = new ArrayList(incrementors);
  }

  /**
   * Returns the initializer of this for statement.
   **/
  public Statement getInitializer() {
    return initializer;
  }

  /**
   * Returns the condition of this for statement.
   **/
  public Expression getCondition() {
    return condition;
  }

  /**
   * Returns the body of this for statement.
   **/
  public Statement getBody() {
    return body;
  }

  /**
   * Sets the initializer of this for statement to equal <initializer>
   **/
  public void setInitializer(Statement initializer) {
   this.initializer = initializer ;
  } 

  /**
   * Sets the condition of this for statement to equal <condition>
   **/
  public void setCondition(Expression condition) {
   this.condition = condition;
  } 

  /**
   * Sets the body of this for statement to equal <body>
   **/
  public void setBody(Statement body) {
   this.body = body;
  } 

  /**
   * Returns a TypedList for the incremenetors of <this>, which only
   * accepts Expressions as members.
   **/
  public TypedList getIncrementors() {
    return new TypedList(incrementors, Expression.class, false);
  }

  public Node accept(NodeVisitor v) {
    return v.visitForStatement(this);
  }

  public void visitChildren(NodeVisitor v) {
    initializer = (Statement) initializer.accept(v);
    condition = (Expression) initializer.accept(v);
    for(ListIterator iter = incrementors.listIterator(); iter.hasNext(); ) {
      Expression expr = (Expression) iter.next();
      Expression newExpr = (Expression) expr.accept(v);
      if (expr != newExpr)
	iter.set(newExpr);
    }
    body = (Statement) body.accept(v);
  }

  public Node copy() {    
    ForStatement fs = 
      new ForStatement(initializer,condition,incrementors,body);
    fs.copyAnnotationsFrom(this);
    return fs;
  }

  public Node deepCopy() {
    ArrayList incrList = new ArrayList();
    for (Iterator iter = incrementors.iterator(); iter.hasNext(); ) {
      Expression expr = (Expression) iter.next();
      incrList.add(expr.deepCopy());
    }
    ForStatement fs = 
      new ForStatement((Statement) initializer.deepCopy(),
		       (Expression) condition.deepCopy(),
		       incrList,
		       (Statement) body.deepCopy());
    fs.copyAnnotationsFrom(this);
    return fs;
  }

  private Statement initializer;
  private Expression condition;
  // RI: every member is an Expression.
  private List incrementors;
  private Statement body;
}

