package jltools.ast;

import jltools.types.*;
import jltools.util.*;

import java.util.*;


/**
 * An immutable representation of a Java language <code>for</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public class ForStatement extends Statement 
{  
  /** 
   * A list of statements that will be executed once before the loop is
   * executed. 
   */
  private List inits;
  private Expression cond;
  /** 
   * A list of statements that will be executed at the end of each iteration. 
   */
  private List iters;
  private Statement body;

  /**
   * Create a new <code>ForStatement</code>.
   *
   * @pre Each element of <code>inits</code> must be a 
   *  <code>Statement</code>. Each element of <code>iters</code> is a
   *  <code>Statement</code>. 
   */
  public ForStatement( List inits, Expression cond, List iters, 
                       Statement body) 
  {
    this.inits = TypedList.copyAndCheck( inits, Statement.class, true);
    this.cond = cond;
    this.iters = TypedList.copyAndCheck( iters, Statement.class, true);
    this.body = body;    
  }

  public ForStatement reconstruct( List inits, Expression cond, List iters, 
                                   Statement body)
  {
    if( this.inits.size() != inits.size() || this.cond != cond
        || this.iters.size() != iters.size() || this.body != body) {
      ForStatement n = new ForStatement( inits, cond, iters, body);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < inits.size(); i++) {
        if( this.inits.get( i) != inits.get( i)) {
          ForStatement n = new ForStatement( inits, cond, iters, body);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      for( int i = 0; i < iters.size(); i++) {
        if( this.iters.get( i) != iters.get( i)) {
          ForStatement n = new ForStatement( inits, cond, iters, body);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }
  
  /**
   * Return an iterator of initializers for this statement.
   *
   * @post Each member of the list is of type <code>Statement</code>.
   */
  public Iterator initializers() 
  {
    return inits.iterator();
  }
 
  /**
   * Returns the condition of this for statement.
   */
  public Expression getCondition() 
  {
    return cond;
  }

  /**
   * Return the list of iterators for this statement.
   *
   * @post Each member of the list is of type <code>Statement</code>.
   */
  public Iterator iterators() 
  {
    return iters.iterator();
  }

  /**
   * Returns the body of this for statement.
   */
  public Statement getBody() 
  {
    return body;
  }

  public void enterScope( LocalContext c)
  {
    c.pushBlock();
  }

  public void leaveScope( LocalContext c)
  {
    c.popBlock();
  }

  /**
   * Visit the children of this node.
   *
   * @pre Requires that the <code>visit</code> method return an object of type
   *  <code>Statement</code> for all initializers and iterators. Also,
   *  <code>cond.visit</code> must return an <code>Expression</code> and
   *  <code>body.visit</code> must return a <code>Statement</code>
   */
  Node visitChildren( NodeVisitor v) 
  {
    List newInits = new ArrayList( inits.size()),
      newIters = new ArrayList( iters.size());

    for( Iterator iter = initializers(); iter.hasNext(); ) {
      Statement stmt = (Statement)((Statement)iter.next()).visit( v);
      if( stmt != null) {
        newInits.add( stmt);
      }
    }

    Expression newCond = null;
    if( cond != null) {
      newCond = (Expression)cond.visit( v);
    }

    for( Iterator iter = iterators(); iter.hasNext(); ) {
      Statement stmt = (Statement)((Statement)iter.next()).visit( v);
      if( stmt != null) {
        newIters.add( stmt);
      }
    }

    Statement newBody = null;
    if( body != null) {
      newBody = (Statement)body.visit( v);
    }

    return reconstruct( newInits, newCond, newIters, newBody);
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    
    if( cond != null && 
         !cond.getCheckedType().isImplicitCastValid( 
                      c.getTypeSystem().getBoolean())) {
      throw new SemanticException("The condition of a for statement must "
                                  + "evaluate to a boolean expression.");
    }

    /*
     * Type checking of the initializers and the iterators is handled by
     * the standard visitor model traversal.
     */

    return this;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    boolean writeSemicolon = true;
    
    w.write( "for( ");

    if( inits != null) {
      for( Iterator iter = inits.iterator(); iter.hasNext(); ) {
        Statement stmt = (Statement)iter.next();
        if( stmt instanceof VariableDeclarationStatement) {
          stmt.translate( c, w);
          writeSemicolon = false;
        } 
        else {
          ((ExpressionStatement)stmt).getExpression().translate(c, w);
        }

        if( iter.hasNext()) {
          w.write( ", ");
        }
      }   
    }

    /*
     * If the initilizer is a single variable declaration statement, then
     * we don't want to write out the semicolon, since the subnode has 
     * already done this.
     */
    if( writeSemicolon) {
      w.write( "; "); 
    }
    
    if( cond != null) {
      cond.translate(c, w);
    }

    w.write ("; " ); /* cond is a expr, so write semicolon. */
    
    if( iters != null) {
      for( Iterator iter = iters.iterator(); iter.hasNext(); ) {
        Statement stmt = (Statement)iter.next();
        if( stmt instanceof ExpressionStatement) {
          ((ExpressionStatement)stmt).getExpression().translate(c, w);      
        }
        else {
          stmt.translate( c, w);
        }
        
        if( iter.hasNext()) {
          w.write( ", ");
        }
      }
    }

    w.write( ") ");
    
    if (body == null) {
      w.write( "; ");
    }
    else if( !(body instanceof BlockStatement)) {
      w.beginBlock();
      body.translate( c, w);
      w.endBlock();
    }
    else {
      body.translate( c, w);
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "( FOR ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}

