/*
 * ForStatement.java
 */

package jltools.ast;

import jltools.types.*;
import jltools.util.*;

import java.util.*;


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
    
    w.write ( "for( " );

    if (initializers != null)
      for ( ListIterator iter = initializers.listIterator(); 
            iter.hasNext(); )
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
    
    if ( condition != null)
      condition.translate(c, w);
    w.write ("; " ); // condition is a expr, so write semicolon.
    if ( incrementors != null)
      for ( ListIterator iter = incrementors.listIterator(); 
            iter.hasNext(); )
      {
        Statement next = (Statement)iter.next();
        if(next instanceof ExpressionStatement)
          ((ExpressionStatement)next).getExpression().translate(c, w);      
        else
          next.translate(c, w);
        
        if (iter.hasNext())
          w.write (", " );
      }
    w.write ( ")" );
    
    if (body == null)
      w.write ( " ; ");
    else if(!(body instanceof BlockStatement))
    {
      w.beginBlock();
      body.translate(c, w);
      w.endBlock();
    }
    else
      body.translate(c, w);
  }

  public Node dump( CodeWriter w)
  {
    w.write( "( FOR ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    
    if ( condition != null && 
         ! condition.getCheckedType().isImplicitCastValid( 
               c.getTypeSystem().getBoolean() ) )
      throw new TypeCheckException(" The conditional must be a boolean.");

    for(ListIterator iter = initializers.listIterator(); iter.hasNext(); ) 
    {
      Statement stat = (Statement) iter.next();
      Annotate.addThrows ( this, Annotate.getThrows( stat ) );
    }

    if ( condition != null)
      Annotate.addThrows ( this, Annotate.getThrows( condition ) );
    if ( incrementors != null)
      for(ListIterator iter = incrementors.listIterator(); 
          iter.hasNext(); ) {
        Statement stat = (Statement) iter.next();
        Annotate.addThrows ( this, Annotate.getThrows ( stat ) );
      }
    if (body != null)
    {
      Annotate.addThrows ( this, Annotate.getThrows ( body ) );
    }
    return this;
  }

  Object visitChildren(NodeVisitor v) 
  {
    Object vinfo = Annotate.getVisitorInfo( this);

    for(ListIterator iter = initializers.listIterator(); iter.hasNext(); ) 
    {
      Statement stat = (Statement) iter.next();
      Statement newStat = (Statement) stat.visit(v);
      vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( newStat), 
                                  vinfo);
      if (stat != newStat)
        iter.set(newStat);
    }
    if ( condition != null)
      condition = (Expression) condition.visit(v);
    if ( incrementors != null)
      for(ListIterator iter = incrementors.listIterator(); 
          iter.hasNext(); ) {
        Statement stat = (Statement) iter.next();
        Statement newStat = (Statement) stat.visit(v);
        vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( newStat), 
                                    vinfo);
        if (stat != newStat)
          iter.set(newStat);
      }
    if (body != null)
    {
      body = (Statement) body.visit(v);
      vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( body), vinfo);
    }
    
    return vinfo;
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

