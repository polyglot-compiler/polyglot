package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * An immutable representation of a Java language <code>while</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */ 
public class WhileStatement extends Statement 
{
  protected final Expression cond;
  protected final Statement body;

  /**
   * Creates a new <code>WhileStatement</code>.
   */
  public WhileStatement( Node ext, Expression cond, Statement body) 
  {
    this.ext = ext;
    this.cond = cond;
    this.body = body;
  }

    public WhileStatement( Expression cond, Statement body) 
    {
	this(null, cond, body);
    }

  /**
   * Lazily reconstruc this node.
   */
  public WhileStatement reconstruct( Node ext, Expression cond, Statement body)
  {
    if( this.cond == cond && this.body == body && this.ext == ext) {
      return this;
    }
    else {
      WhileStatement n = new WhileStatement( ext, cond, body);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the expression that this statement is conditioned on.
   */
  public Expression getCondition() 
  {
    return cond;
  }

  /**
   * Returns the body associated with this statement.
   */
  public Statement getBody() 
  {
    return body;
  }

  /** 
   * Visit the children of this node.
   *
   * @pre Requires that <code>cond.visit</code> returns an object of type
   *  <code>Expression</code> and that, if <code>body</code> is non-
   *  <code>null</code>, <code>body.visit</code> returns an object
   *  of type <code>Statement</code>.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v), (Expression)cond.visit( v),
                        (Statement)Node.condVisit(body, v));
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type ctype = cond.getCheckedType();
    
    if( !ctype.equals( c.getTypeSystem().getBoolean())) {
      throw new SemanticException( "Condition expression of while statement "
                                    + "must have boolean type.",
				   Annotate.getLineNumber(cond));
    }
    
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "while (" );
    cond.translate_block( c, w);
    w.write( ")");

    if( body == null)
	w.write( " ;");
    else
	body.translate_substmt(c, w);
  }

  public void dump( CodeWriter w)
  {
    w.write( "WHILE ");
    dumpNodeInfo( w);
  }
}
