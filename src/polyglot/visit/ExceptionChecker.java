package jltools.visit;

import jltools.ast.*;
import jltools.util.*;
import jltools.types.*;
import java.util.*;


/**
 * DOCME
 */
public class ExceptionChecker extends NodeVisitor
{  
  TypeSystem ts;
  SubtypeSet s;
  ErrorQueue eq;
  protected List oldExceptionCheckers;

  public ExceptionChecker( TypeSystem ts, ErrorQueue eq )
  {
    this.ts = ts;
    this.eq = eq;
    oldExceptionCheckers = new Vector();
    s = new SubtypeSet();
  }

  protected ExceptionChecker (TypeSystem ts, ErrorQueue eq, List oldExceptionCheckers)
  {
    s = new SubtypeSet();
    this.ts = ts;
    this. eq = eq; 
    this.oldExceptionCheckers = oldExceptionCheckers;
  }

  public TypeSystem getTypeSystem() {
    return ts;
  }

  public Node override( Node n)
  {
    if ( n instanceof TryStatement)
    {
      // do something special. this is because the children of a 
      // try block (namely the catch block) has to be handled a little 
      // bit differently.

      TryStatement ts = (TryStatement)n;
      ExceptionChecker ec = getExceptionChecker();

      ts.getTryBlock().visit( ec );
      try
      {
        ts.exceptionCheck( ec );
      }
      catch ( SemanticException se)
      {
        throw new InternalCompilerError(n, "Unexpected Semantic Exception.  "
                                       +"It appears that type comparisons"
                                       + "are not working in the " +
                                        "TypeSystem.");
      }
      // ec now contains any exceptions that the catch 
      // blocks didn't get to.
      s.addAll ( ec.s );
      releaseExceptionChecker(ec );

      for ( Iterator i = ts.catchBlocks(); i.hasNext() ; )
      {
        ExceptionChecker ecBlock = getExceptionChecker();
        ((Node)i.next()).visit( ecBlock ) ;
        s.addAll( ecBlock.s);
        releaseExceptionChecker(ecBlock);
      }

      if ( ts.getFinallyBlock() != null) 
      {
        ExceptionChecker ecBlock = getExceptionChecker();
        ts.getFinallyBlock().visit ( ec );
        s.addAll( ecBlock.s);
        releaseExceptionChecker(ecBlock);
      }

      // now, try statement is done. 
      return n;
    }
    return null;
  }
  
  /**
   * This method is called when we are to perform a "normal" traversal of 
   * a subtree rooted at <code>n</code>.   At every node, we will push a 
   * stack frame.  Each child node will add the exceptions that it throws
   * to this stack frame. For most nodes ( excdeption for the try / catch)
   * will just aggregate the stack frames.
   *
   * @param n The root of the subtree to be traversed.
   * @return The <code>NodeVisitor</code> which should be used to visit the 
   *  children of <code>n</code>.
   *
   */
  public NodeVisitor enter( Node n)
  {
    return getExceptionChecker(); 
  }

  /**
   * Here, we pop the stack frame that we pushed in enter and agregate the 
   * exceptions.
   *
   * @param old The original state of root of the current subtree.
   * @param n The current state of the root of the current subtree.
   * @param v The <code>NodeVisitor</code> object used to visit the children.
   * @return The final result of the traversal of the tree rooted at 
   *  <code>n</code>.
   */
  public Node leave( Node old, Node n, NodeVisitor v)
  {
    ExceptionChecker oldEC = (ExceptionChecker)v ;
    // merge results from the children.
    s.addAll ( oldEC.s );

    releaseExceptionChecker( oldEC );


    // gather exceptions from this node.
    try
    {
      return n.exceptionCheck( this );
    }
    catch ( SemanticException se)
    {
        throw new InternalCompilerError("Unexpected Semantic Exception.  "
                                       +"It appears that type comparisons"
                                       + "are not working in the " +
                                        "TypeSystem.");
    }
  }

  /**
   * The ast nodes will use this callback to notify us that they throw an 
   * exception of type t. This should only be called by MethodExpr node, 
   * and throw node, since they are the only node which can generate
   * exceptions.  
   *
   * @param t The type of exception that the node throws.
   */
  public void throwsException (ClassType t)
  {
    s.add ( t ) ;
  }
  
  /**
   * Method to allow the throws clause and method body to inspect and
   * modify the throwsSet.
   */
  public SubtypeSet getThrowsSet()
  {
    return s;
  }

  protected ExceptionChecker getExceptionChecker ()
  {
    ExceptionChecker e = null;
    synchronized ( oldExceptionCheckers )
    {
      int size = oldExceptionCheckers.size() ; 
      if ( size > 0 )
        e = (ExceptionChecker)oldExceptionCheckers.remove( 
                    oldExceptionCheckers.size() -1 ) ;
    }
    if ( e != null) return e;
    return new ExceptionChecker(ts, eq, oldExceptionCheckers); 
  }
  
  protected void releaseExceptionChecker(ExceptionChecker oldEC)
  {
    // reuse the ExceptionCheckers. saves an allocation.  
    oldEC.s.clear();
    synchronized ( oldExceptionCheckers ) 
    {
      oldExceptionCheckers.add ( oldEC );
    }    
  }

  public void reportError( String description, Position position)
  {
    eq.enqueue( ErrorInfo.SEMANTIC_ERROR, description, position);    
  }
  
}
