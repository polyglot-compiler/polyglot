package jltools.ast;

import jltools.util.*;
import jltools.types.*;

import java.util.*;


/**
 * A <code>BlockStatement</code> represents a Java block statement -- a 
 * immutable sequence of statements.
 */
public class BlockStatement extends Statement {
 
  protected final List statements;

  /**
   * Create a new, empty BlockStatement.
   */
  public BlockStatement(Node ext) 
  {
    this.ext = ext;
    statements = new ArrayList();
  }

    public BlockStatement() {
	this((Node)null);
    }

  /**
   * Create a new <code>BlockStatement</code> with <code>statementList</code>
   * as its statements.
   *
   * @pre Each element in <code>list</code> is a <code>Statement</code>.
   */
  public BlockStatement( Node ext, List list) 
  {
    this.ext = ext;
    statements = TypedList.copyAndCheck( list, Statement.class, true);
  }

    public BlockStatement( List list) {
	this(null, list);
    }

  /**
   * Lazily reconstuct this node.
   */
  public BlockStatement reconstruct( Node ext, List list)
  {
    if( statements.size() != list.size() || this.ext != ext) {
      BlockStatement n = new BlockStatement( ext, list);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < list.size(); i++) {
        if( list.get( i) != statements.get( i)) {
          BlockStatement n = new BlockStatement( ext, list);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }

    public BlockStatement reconstruct( List list) {
	return reconstruct(this.ext, list);
    }

  /** 
   * Returns a <b>new</b> <code>BlockStatement</code> object which has one
   * additional statement in it. Namely the argument passed to this method.
   */
  public BlockStatement appendStatement( Statement stmt) 
  {
    List newStatements = new ArrayList( statements);
    newStatements.add( stmt);
    return reconstruct( newStatements);
  }

  /**
   * Returns the child at position <code>pos</code>.
   */
  public Statement statementAt( int pos) 
  {
    return (Statement)statements.get(pos);
  }

  /**
   * Returns an <code>Iterator</code> which will yield every statement in this
   * in order.
   */
  public Iterator statements() {
    return new TypedListIterator(statements.listIterator(), 
				 Statement.class,
				 true);
  }

  /**
   * Visit the children (statements) of this node.
   *
   * @pre Requires that the <code>visit</code> method of each child returns
   *  an object of type <code>Expression</code>.
   * @post Returns this node if there are no changes to the children. Otherwise
   *  return a reconstructed copy of this node with the appropriate changes.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    List list = new ArrayList( statements.size());

    for( Iterator iter = statements(); iter.hasNext(); ) {
      Statement stmt = (Statement)((Statement)iter.next()).visit( v);
      if( stmt != null) {
        list.add( stmt);
      }
    }
    return reconstruct( Node.condVisit(this.ext, v), list);
  }

  public void enterScope( LocalContext c)
  {
    c.pushBlock();
  }

  public void leaveScope( LocalContext c) 
  {
    c.popBlock();
  }
  
  public Node typeCheck( LocalContext c) throws SemanticException
  {
    return this;
  }

  public void translate_substmt(LocalContext c, CodeWriter w) {
      w.write(" ");
      translate(c, w);
  }

  public String toString() {
    String s = "";
    for (Iterator it = statements.iterator(); it.hasNext(); ) 
    {
      s += it.next();
      if (it.hasNext())
        s += " ";
    }
    return "{" + s + "}";
  }

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    enterScope(c);
    w.write("{");
    w.allowBreak(4," ");
    w.begin(0);
    for (ListIterator it = statements.listIterator(); it.hasNext(); ) 
    {
      ((Node)it.next()).translate_block(c, w);
      if (it.hasNext())
        w.newline(0);
    }
    w.end();
    w.allowBreak(0, " ");
    w.write("}");
    leaveScope(c);
  }

  public void dump( CodeWriter w)
  {
    w.write( "BLOCK ");
    dumpNodeInfo( w);
  }
}

