/*
 * ForStatement.java
 */

package jltools.ast;

import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import jltools.types.Context;
import jltools.util.CodeWriter;
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


  public void translate(Context c, CodeWriter w)
  {
    w.write ( " for ( " );
    initializer.translate(c, w);
    // don't have to write a semicolon because initializer is a statemnt
    condition.translate(c, w);
    w.write (" ; " ); // condition is a expr, so write semicolon.
    for ( ListIterator iter = incrementors.listIterator(); iter.hasNext(); )
    {
      ((Expression)iter.next()).translate(c, w);
      if (iter.hasNext())
        w.write (", " );
    }
    w.write ( " ) " );
    w.beginBlock();
    body.translate(c, w);
    w.endBlock();
  }

  public void dump(Context c, CodeWriter w)
  {
    w.write (" ( FOR " );
    initializer.dump(c, w);
    condition.dump(c, w);
    for (ListIterator iter = incrementors.listIterator(); iter.hasNext(); )
    {
      ((Expression)iter.next()).dump(c, w);
    }
    w.beginBlock();
    body.dump(c, w);
    w.write (" ) ");
    w.endBlock();
  }

  public Node typeCheck(Context c)
  {
    // FIXME; implement
    return this;
  }

  public void visitChildren(NodeVisitor v) {
    initializer = (Statement) initializer.visit(v);
    condition = (Expression) initializer.visit(v);
    for(ListIterator iter = incrementors.listIterator(); iter.hasNext(); ) {
      Expression expr = (Expression) iter.next();
      Expression newExpr = (Expression) expr.visit(v);
      if (expr != newExpr)
	iter.set(newExpr);
    }
    body = (Statement) body.visit(v);
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

