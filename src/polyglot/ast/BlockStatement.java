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
  public BlockStatement() 
  {
    statements = new ArrayList();
  }

  /**
   * Create a new <code>BlockStatement</code> with <code>statementList</code>
   * as its statements.
   *
   * @pre Each element in <code>list</code> is a <code>Statement</code>.
   */
  public BlockStatement( List list) 
  {
    statements = TypedList.copyAndCheck( list, Statement.class, true);
  }

  /**
   * Lazily reconstuct this node.
   */
  public BlockStatement reconstruct( List list)
  {
    if( statements.size() != list.size()) {
      BlockStatement n = new BlockStatement( list);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < list.size(); i++) {
        if( list.get( i) != statements.get( i)) {
          BlockStatement n = new BlockStatement( list);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
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
  Node visitChildren( NodeVisitor v) 
  {
    List list = new ArrayList( statements.size());

    for( Iterator iter = statements(); iter.hasNext(); ) {
      Statement stmt = (Statement)((Statement)iter.next()).visit( v);
      if( stmt != null) {
        list.add( stmt);
      }
    }
    return reconstruct( list);
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

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write("{");
    w.beginBlock();
    for (ListIterator it = statements.listIterator(); it.hasNext(); ) 
    {
      ((Node)it.next()).translate(c, w);
      if (it.hasNext())
        w.newline(0);
    }
    w.endBlock();
    w.write("}");
  }

  public void dump( CodeWriter w)
  {
    w.write( "( BLOCK ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}

