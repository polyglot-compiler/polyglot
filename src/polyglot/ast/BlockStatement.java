/*
 * BlockStatement.java
 */

package jltools.ast;

import jltools.util.*;
import jltools.types.*;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * BlockStatement
 *
 * Overview: A BlockStatement represents a Java block statement -- a mutable
 *   sequence of statements.
 **/
public class BlockStatement extends Statement {
  /**
   * Effects: Create a new, empty BlockStatement.
   **/
  public BlockStatement() {
    statements = new ArrayList();
  }

  /**
   * Requires: every element of statementList is a Statement.
   * Effects: Create a new BlockStatement with <statementList> as its
   * statements.
   **/
  public BlockStatement(List statementList) {
    TypedList.check(statementList, Statement.class);
    statements = new ArrayList(statementList);
  }

  /**     
   * Add a new child <s> to this BlockStatement.
   **/
  public void addStatement(Statement s) {
    statements.add(s);
  }
  
  /**
   * Adds a new child <s> to this BlockStatement, such that the child
   * is at position <pos>.  Throws an IndexOutOfBoundsException if <pos>
   * is not a valid position.
   **/
  public void addStatement(Statement s, int pos) {
    statements.add(pos, s);
  }
 
  /**
   * Removes a child at position <pos> from this BlockStatement.
   * Throws an IndexOutOfBoundsException if <pos> is not a valid
   * position.
   **/
  public void removeStatement(int pos) {
    statements.remove(pos);
  }

  /**
   * Returns the child at position <pos>.  Throws an
   * IndexOutOfBoundsException if <pos> is not a valid position.
   **/
  public Statement statementAt(int pos) {
    return (Statement) statements.get(pos);
  }

  /**
   * Returns a TypedListIterator which will yield every statement in this
   * in order, and only allow Statements to be inserted.
   **/
  public TypedListIterator iterator() {
    return new TypedListIterator(statements.listIterator(), 
				 Statement.class,
				 false);
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

  public Node dump( CodeWriter w)
  {
    w.write( "( BLOCK ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node adjustScope( LocalContext c)
  {
    c.pushBlock();
    return null;
  }
  
  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    for (Iterator i = statements.iterator(); i.hasNext(); )
    {
      Node n = (Node)i.next();
      Annotate.addThrows( this, Annotate.getThrows (n) );
      if ( Annotate.terminatesOnAllPaths(n) ) 
      {
        Annotate.setTerminatesOnAllPaths (this, true);
        if ( i.hasNext())
          throw new TypeCheckException( "This statement is unreachable.", 
                                        Annotate.getLineNumber( (Node)i.next() ) );
      }
    }
    c.popBlock();
    return this;
  }

  /**
   * Requires: v will not transform a Statement into anything other than
   *    a Statement, an Expression, or Null.
   * Effects:
   *    Visits the children of this in order with <v>.  If <v> returns null,
   *    the statement is elided.  If it returns an Expression, it is wrapped
   *    as an ExpressionStatement.
   **/
  Object visitChildren(NodeVisitor v) {
    return visitChildren(v, false);
  }

  /**
   * Requires: v will not transform a Statement into anything other than
   *    a Statement, an Expression, or Null.
   *
   * Effects:
   *    Visits the children of this in order with <v>.  If <v> returns
   *    null, the statement is elided.  If it returns an Expression, it is
   *    wrapped as an ExpressionStatement.
   *
   *    If <flatten> is true, all BlockStatements have their contents
   *    contents are interpolated into this statement.
   **/
  Object visitChildren(NodeVisitor v, boolean flatten) 
  {
    Object vinfo = Annotate.getVisitorInfo( this);

    for (ListIterator it = statements.listIterator(); it.hasNext(); ) {
      Node node = (Node) it.next();
      Node newNode = node.visit(v);
      if (newNode == null) {
	// Remove the node.
	it.remove();
	continue;
      } else if (flatten && newNode instanceof BlockStatement) {
	// Interpolate the subnodes.
	it.remove();
	BlockStatement bs = (BlockStatement) newNode;
	for (Iterator bsIt = bs.statements.iterator(); bsIt.hasNext(); ) {
	  it.add(bsIt.next());
	}
      } else if (node != newNode) {
	// The node changed.
        vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( newNode), vinfo);
	if (newNode instanceof Expression) {
	  it.set(new ExpressionStatement((Expression) newNode));
	} else {
	  Assert.assert(newNode instanceof Statement);  
	  it.set(newNode);
	}
      } else {
        // The node hasn't changed. 
        vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( newNode), vinfo);
      }
    }    
    return vinfo;
  }

  public Node copy() {
    BlockStatement bs = new BlockStatement();
    bs.copyAnnotationsFrom(this);
    for (Iterator i = statements.iterator(); i.hasNext(); ) {
      bs.addStatement((Statement) i.next());
    }
    return bs;
  }

  public Node deepCopy() {
    BlockStatement bs = new BlockStatement();
    bs.copyAnnotationsFrom(this);
    for (Iterator i = statements.iterator(); i.hasNext(); ) {
      Statement s = (Statement) i.next();
      bs.addStatement((Statement) s.deepCopy());
    }
    return bs;
  }

  // RI: every member of statements is a Statement.
  private ArrayList statements;
}

