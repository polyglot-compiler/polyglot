package jltools.ast;

import jltools.types.*;
import jltools.util.*;

/**
 * Am immutable representation of a Java statement with a
 * label.  A labeled statement contains the statement being labelled
 * and a string label.
 */
public class LabelledStatement extends Statement 
{
  protected final String label;
  protected final Statement stmt;

  /**
   * Creates a new <code>LabelledStatement</code>.
   */
  public LabelledStatement( Node ext, String label, Statement stmt) 
  {
    this.ext = ext;
    this.label = label;
    this.stmt = stmt;
  }

    public LabelledStatement( String label, Statement stmt) {
	this(null, label, stmt);
    }

  /**
   * Lazily reconstruct this node.
   */
  public LabelledStatement reconstruct( Node ext, String label, Statement stmt)
  {
    if( this.label.equals( label) && this.stmt == stmt && this.ext == ext) {
      return this;
    }
    else {
      LabelledStatement n = new LabelledStatement( ext, label, stmt);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  public LabelledStatement reconstruct( String label, Statement stmt) {
      return reconstruct(this.ext, label, stmt);
  }

  /**
   * Returns the label associated with this statement.
   */
  public String getLabel() 
  {
    return label;
  }

  /**
   * Returns the statement being labelled in this statement. 
   */
  public Statement getStatement() 
  {
    return stmt;
  }

  /**
   * Visit the children of this node.
   *
   * @pre Requires that <code>stmt.visit</code> transforms itself into an 
   *  object of type <code>Statement</code>.
   */
  public Node visitChildren(NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v),label, (Statement)stmt.visit( v));
  }

  public Node typeCheck(LocalContext c)
  {
    /* Nothing to do. */
    return this;
  }

  // FIXME implement flowCheck

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( label + ": ");
    stmt.translate( c, w);
  }

  public void dump( CodeWriter w)
  {
    w.write( "( LABEL");
    w.write( " < " + label + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
    
