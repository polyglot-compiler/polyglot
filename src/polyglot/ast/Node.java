package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.util.*;
import java.io.IOException;

/**
 * A <code>Node</code> is an AST node.  All other nodes in the AST must be 
 * subclasses of <code>Node</code>. Nodes are intended to implement <i>lazy
 * reconstruction</i>. Thus node fields should be immutable. It is recommended
 * that all subclasses of <code>Node</code> implement a method of the 
 * following form, to assist in this reconstuction.
 *
 * <pre><code>
 * public MyNode reconstruct( Node ext, Expression field1, TypeNode field2)
 * {
 *   if( this.field1 == field1 && this.field2 == field2 && this.ext == ext) {
 *     return this;
 *   }
 *   else {
 *     MyNode n = new MyNode( field1, field2, ext.reconstruct());
 *     n.copyAnnotationsFrom( this);
 *     return n;
 *   }
 * }
 * </code></pre>
 *
 * By convention, <code>reconstruct</code> methods determine equality by:
 * <ul>
 * <li><code>equals</code> for fields of type <code>String</code>
 * <li>pointer equality (<code>==</code>) of elements of fields of type 
 *  <code>List</code>, <code>Set</code>, or <code>Collection</code>
 * <li>pointer equality (<code>==</code>) of all other reference fields
 * <li>equality for all primitive fields.
 * </ul>
 *
 * @see jltools.ast.NodeVisitor
 */
public abstract class Node extends AnnotatedObject {

    /**
     * The extension object for this node.
     */
    public Node ext;

    /**
     * Determines whether this node has an extension object.
     */
    public boolean isExtended() {
	return ext == null;
    }

    public Node() {
	this(null);
    }

    public Node(Node ext) {
	this.ext = ext;
    }


  /**
   * The main entry point to the AST when using <code>NodeVisitor</code>s to
   * traverse the tree.
   * 
   * @param v The visitor which will traverse/rewrite the tree.
   * @return A new node which represents the AST if a change was made; 
   *  otherwise <code>this</code>.
   */
  public final Node visit( NodeVisitor v)
  {
    Node n = v.override( this);

    if( n != null) {
      return n;
    }
    else {
      NodeVisitor v_ = v.enter( this);
      n = visitChildren( v_);
      return v.leave( this, n, v_);
    }
  }

  /**
   * Defines the fashion in which the AST traversed. That is, each node 
   * should override this method and call <code>visit</code> on each of its
   * children. If any of the children change (i.e. if they return a new node
   * from <code>visit</code>) then this method should return a copy of the
   * current node with the appropriate field set to the new child.
   * 
   * @see jltools.ast.Node#visit
   *
   * @param v The visitor which is currently traversing the tree.
   * @return A new node if any changes where made to this node (or its 
   *  children; otherwise <code>this</code>.
   */
  protected abstract Node visitChildren(NodeVisitor v);

    protected static Node condVisit(Node n, NodeVisitor v) {
	if (n != null) {
	    return n.visit( v);
	} else {
	    return null;
	}
    }

  /**
   * Collects classes, methods, and fields from an AST. The types of fields
   * as well as the return, argument, and exception types of the methods
   * may be ambiguous.
   *
   * @param sr The visitor which allows nodes to add new symbols.
   * @return See notes for <code>visit</code> and <code>visitChildren</code>.
   */
  public abstract Node readSymbols( SymbolReader sr) throws SemanticException;

  /**
   * Adjust the environment for entering a new scope.
   */
  public void enterScope( LocalContext c) {}

  /**
   * Adjust the environment for leaving the current scope.
   */
  public void leaveScope( LocalContext c) {}
  
  /**
   * Remove any remaining ambiguities from the AST.
   *
   * @return See notes for <code>visit</code> and <code>visitChildren</code>.
   */
  public Node removeAmbiguities( LocalContext c) throws SemanticException
  { 
    return this; 
  }

  /**
   * Cleanup class signatures in the AST.
   *
   * @return See notes for <code>visit</code> and <code>visitChildren</code>.
   */
  public Node cleanupSignatures(LocalContext c, SignatureCleaner sc)
    throws SemanticException, IOException
  { 
    return null; 
  }

  /**
   * Fold all constants.
   *
   * @return The node with all constants folded in.
   */
  public Node foldConstants(ExtensionFactory ef) 
  {
    return this;
  }
  
  /**
   * Check the AST to ensure that expressions and statements follow the 
   * rules of the established type system. 
   * 
   * @return See notes for <code>visit</code> and <code>visitChildren</code>.
   */
  public abstract Node typeCheck( LocalContext c) throws SemanticException;

  /**
   * Check that exceptions are properly propagated throughout the tree.
   *
   * @return See notes for <code>visit</code> and <code>visitChildren</code>.
   */
  public Node exceptionCheck( ExceptionChecker ec) throws SemanticException 
  {
    return this; 
  }

  /**
   * Check the AST to ensure that nodes are reachable and that blocks of code
   * complete correctly. Also check labeled statements and branching 
   * statements.
   *
   * @return See notes for <code>visit</code> and <code>visitChildren</code>.
   */
  public Node flowCheck( FlowChecker fc) throws SemanticException 
  {
    return this; 
  }

  /**
   * Translate the AST using the given <code>CodeWriter</code>.
   * <p>
   * Note that this method does <b>not</b> use the visitor model of traversal.
   * Rather, it uses a traditional recursive traversal.
   */
  public abstract void translate( LocalContext c, CodeWriter w);
  public void translate_block( LocalContext c, CodeWriter w) {
    w.begin(0);
    translate(c, w);
    w.end();
  }

    /** Adds an explicit THIS SpecialExpression to every FieldExpression with null target
     */
    public Node addThis() {
	return this;
    }

  
  /**
   * Print relevant debugging info for this node to the writer. (This method
   * should <b>not</b> print out infomation for child nodes. They will be
   * traversed using <code>visitChildren</code>. That way, this method may
   * also be used to debug the traversal process itself.)
   * <p>
   * It is also recommended that nodes call <code>dumpNodeInfo</code> to
   * print out generic info.
   *
   * @param w The output writer used to display information.
   */
  public abstract void dump( CodeWriter w) throws SemanticException;

  /**
   * Dumps annotations to the writer, if any annotations have been set.
   */
  public void dumpNodeInfo( CodeWriter w)
  {
    Type type1 = Annotate.getCheckedType( this);
    Type type2 = Annotate.getExpectedType( this);

    //    w.write( "C: " + getClass().getName() + " ");

    if( type1 != null && type2 != null ) {
      w.begin(0);
      w.write("T: " + type1.getTypeString());
      w.allowBreak(0);
      w.write("E: " + type2.getTypeString());
      w.end();
    } else if (type1 != null) {
      w.write("T: " + type1.getTypeString());
    } else if (type2 != null) {
      w.write("E: " + type2.getTypeString());
    }
  }
}
