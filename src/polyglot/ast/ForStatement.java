/*
 * ForStatement.java
 */

package jltools.ast;

import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import jltools.types.LocalContext;
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
   *    initializers <initializers>, condition <condition>, and incrementors
   *    <incrementors>.
   */
  public ForStatement(List initializers,
		      Expression condition,
		      List incrementors,
		      Statement body) {

    TypedList.check(initializers, Statement.class);
    this.initializers = new ArrayList(initializers);
   
    this.condition = condition;
    
    TypedList.check(incrementors, Statement.class);    
    this.incrementors = new ArrayList(incrementors);
    
    this.body = body;    
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
    return new TypedList(initializers, Statement.class, false);
  }

  /**
   * Returns a TypedList for the incremenetors of <this>, which only
   * accepts Statement as members.
   **/
  public TypedList getIncrementors() {
    return new TypedList(incrementors, Statement.class, false);
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    boolean writeSemicolon = true;
    
    w.write ( "for ( " );
    
    for ( ListIterator iter = initializers.listIterator(); iter.hasNext(); )
    {
      Statement next = (Statement)iter.next();
      if(next instanceof VariableDeclarationStatement) {
        next.translate(c, w);
        writeSemicolon = false;
      } else {
        ((ExpressionStatement)next).getExpression().translate(c, w);      
      }
          
      if (iter.hasNext())
        w.write (", " );
    }

    
    // If the initilizer is a single variable declaration statement, then
    // we don't want to write out the semicolon, since the subnode has 
    // already done this.
    if( writeSemicolon) {
      w.write ("; " ); 
    }

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

  public void dump(LocalContext c, CodeWriter w)
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

  public Node typeCheck( LocalContext c)
  {
    // FIXME; implement
    return this;
  }

  public void visitChildren(NodeVisitor v) {
    for(ListIterator iter = initializers.listIterator(); iter.hasNext(); ) {
      Statement stat = (Statement) iter.next();
      Statement newStat = (Statement) stat.visit(v);
      if (stat != newStat)
        iter.set(newStat);
    }
    condition = (Expression) condition.visit(v);
    for(ListIterator iter = incrementors.listIterator(); iter.hasNext(); ) {
      Statement stat = (Statement) iter.next();
      Statement newStat = (Statement) stat.visit(v);
      if (stat != newStat)
        iter.set(newStat);
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
  private Expression condition;
  // RI: every member is a Statement.
  private List incrementors;
  private Statement body;
}

