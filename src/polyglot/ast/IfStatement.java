package jltools.ast;

import jltools.types.*;
import jltools.util.*;


/**
 * An immutable representation of a Java language <code>if</code> statement.
 * Contains an expression whose value is tested, a ``then'' statement 
 * (consequent), and optionally an ``else'' statement (alternate).
 */
public class IfStatement extends Statement 
{
  protected final Expression cond;
  protected final Statement then;
  protected final Statement else_;

  /**
   * Creates a new <code>IfStatement</code>.
   * 
   * @param cond The conditional expression to be tested.
   * @param then The consequent.
   * @param else_ The alternate. This parameter may be <code>null</code>.
   */
  public IfStatement(Expression cond, Statement then, Statement else_) 
  {
    this.cond = cond;
    this.then = then;
    this.else_ = else_;
  }

  public IfStatement(Expression cond, Statement then) 
  {
    this( cond, then, null);
  }

  /**
   * Lazily reconstruct this node.
   */
  public IfStatement reconstruct(Expression cond, Statement then, 
                                 Statement else_) 
  {
    if( this.cond == cond && this.then == then && this.else_ == else_) {
      return this;
    }
    else {
      IfStatement n = new IfStatement( cond, then, else_);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the conditional expression for this statement.
   */
  public Expression getCondition() 
  {
    return cond;
  }

  /**
   * Returns the then statement associated with this <code>IfStatement</code>.
   */
  public Statement getConsequent() 
  {
    return then;
  }

  /**
   * Returns the else statement associated with this <code>IfStatement</code>.
   */
  public Statement getAlternate() 
  {
    return else_;
  }

  /**
   * Visit the children of this node.
   * 
   * @pre Requires that <code>cond.visit</code> transforms the condition
   *  into an object of type <code>Expression</code> and that the
   *  <code>visit</code> method for the consequent and the alternate
   *  should transform each into objects of type <code>Statement</code>.
   */
  public Node visitChildren(NodeVisitor v) 
  {
    return reconstruct( (Expression)cond.visit( v),
                        (Statement)then.visit( v),
                        (else_ == null ? null : (Statement)else_.visit( v)));
  }

  public Node typeCheck(LocalContext c) throws SemanticException
  {
    Type ctype = cond.getCheckedType();

    if( !ctype.equals( c.getTypeSystem().getBoolean())) {
      throw new SemanticException( "Conditional must have boolean type.");
    }

    return this;
  }

  public void translate(LocalContext c, CodeWriter w)
  {    
    w.write( "if (");
    cond.translate_block(c, w);
    w.write( ")");
   
    then.translate_substmt(c, w);

    if ( else_ != null) {
      if (then instanceof BlockStatement) // allow the "} else {" formatting
	w.write(" ");
      else
	w.allowBreak(0, " ");
      w.write ( "else");
      else_.translate_substmt(c, w);
    }
  }

  public void dump(CodeWriter w)
  {
    w.write( "( IF ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
