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
  public ForStatement(List initializers,
		      Expression condition,
		      List incrementors,
		      Statement body) {
    
    Iterator i = initializers.iterator();
    if(initializers.size() == 1)
    {
      Object o = i.next();
      if(o instanceof VariableDeclarationStatement)
      {
        this.initializer = (VariableDeclarationStatement)o;
        this.hasSingleInitializer = true;
      }
      else
      {
        TypedList.check(initializers, Statement.class);
        this.initializers = new ArrayList(initializers);
        this.hasSingleInitializer = false;
      }
    }
    else
    {
      TypedList.check(initializers, Statement.class);
      this.initializers = new ArrayList(initializers);
      this.hasSingleInitializer = false;
    }

    TypedList.check(incrementors, Statement.class);    
    this.condition = condition;
    this.body = body;
    this.incrementors = new ArrayList(incrementors);
  }
  
  public ForStatement(Statement initializer,
            Expression condition,
            List incrementors,
            Statement body) {
      TypedList.check(incrementors, Statement.class);
      this.initializer = initializer;
      this.hasSingleInitializer = true;
      this.condition = condition;
      this.body = body;
      this.incrementors = new ArrayList(incrementors);
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

  public TypedList getInitializers() {
    if(hasSingleInitializer) {
      TypedList l = new TypedList(null, Statement.class, false);
      l.add(initializer);
      return l;
    }
    else
      return new TypedList(initializers, Statement.class, false);
  }

  /**
   * Returns a TypedList for the incremenetors of <this>, which only
   * accepts Statement as members.
   **/
  public TypedList getIncrementors() {
    return new TypedList(incrementors, Statement.class, false);
  }


  public void translate(Context c, CodeWriter w)
  {
    w.write ( " for ( " );
    if(hasSingleInitializer)
    {
      initializer.translate(c, w);  
    }
    else
    {
      for ( ListIterator iter = initializers.listIterator(); iter.hasNext(); )
      {
        Statement next = (Statement)iter.next();
        if(next instanceof ExpressionStatement)
          ((ExpressionStatement)next).getExpression().translate(c, w);      
        else
          next.translate(c, w);
          
        if (iter.hasNext())
          w.write (", " );
      }
      w.write ("; " ); 
    }
    // don't have to write a semicolon because initializer is a statemnt
    // except it is, so we do have to.
    
    condition.translate(c, w);
    w.write ("; " ); // condition is a expr, so write semicolon.
    for ( ListIterator iter = incrementors.listIterator(); iter.hasNext(); )
    {
      Statement next = (Statement)iter.next();
      if(next instanceof ExpressionStatement)
        ((ExpressionStatement)next).getExpression().translate(c, w);      
      else
        next.translate(c, w);
        
      if (iter.hasNext())
        w.write (", " );
    }
    w.write ( " ) " );
    
    if(!(body instanceof BlockStatement))
    {
      w.beginBlock();
      body.translate(c, w);
      w.endBlock();
    }
    else
      body.translate(c, w);
  }

  public void dump(Context c, CodeWriter w)
  {
    w.write (" ( FOR " );
    for (ListIterator iter = initializers.listIterator(); iter.hasNext(); )
    {
      ((Statement)iter.next()).dump(c, w);
    }    
    condition.dump(c, w);
    for (ListIterator iter = incrementors.listIterator(); iter.hasNext(); )
    {
      ((Statement)iter.next()).dump(c, w);
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
    for(ListIterator iter = initializers.listIterator(); iter.hasNext(); ) {
      Expression expr = (Expression) iter.next();
      Expression newExpr = (Expression) expr.visit(v);
      if (expr != newExpr)
        iter.set(newExpr);
    }
    condition = (Expression) condition.visit(v);
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
      new ForStatement(initializers,condition,incrementors,body);
    fs.copyAnnotationsFrom(this);
    return fs;
  }

  public Node deepCopy() {
    ArrayList initList = new ArrayList();
    ArrayList incrList = new ArrayList();
    for (Iterator iter = initializers.iterator(); iter.hasNext(); ) {
      Expression expr = (Expression) iter.next();
      initList.add(expr.deepCopy());
    }
    for (Iterator iter = incrementors.iterator(); iter.hasNext(); ) {
      Expression expr = (Expression) iter.next();
      incrList.add(expr.deepCopy());
    }
    ForStatement fs =
      new ForStatement(initList,
		       (Expression) condition.deepCopy(),
		       incrList,
		       (Statement) body.deepCopy());
    fs.copyAnnotationsFrom(this);
    return fs;
  }
  
  // RI: every member is a Statement.
  private List initializers;
  private Statement initializer;
  private boolean hasSingleInitializer;
  
  private Expression condition;
  // RI: every member is a Statement.
  private List incrementors;
  private Statement body;
}

