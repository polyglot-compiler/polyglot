/*
 * TryStatement.java
 */

package jltools.ast;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Overview: An immutable representation of a try block, one or more
 * catch blocks, and an optional finally block.
 */
public class TryStatement extends Statement {
    
  /**
   * Requires: <code>catchBlocks</code> contains only elements of
   * type CatchStatement
   *
   * Effects: Creates a new TryStatement with <code>tryBlock</code> as 
   * the try block, the catch blocks in <code>catchBlocks</code> and a 
   * finally block of <code>finallyBlock</code>.  If there is no finally
   *  block, then <code>finallyBlock</code> should be null.
   */
  public TryStatement(Node ext, BlockStatement tryBlock,
		      List catchBlocks,
		      BlockStatement finallyBlock) {
    this.ext = ext;
    this.tryBlock = tryBlock;
    this.finallyBlock = finallyBlock;
    TypedList.check(catchBlocks, CatchBlock.class);
    this.catchBlocks = new ArrayList(catchBlocks);
  }

  public TryStatement(BlockStatement tryBlock,
		      List catchBlocks,
		      BlockStatement finallyBlock) {
      this(null, tryBlock, catchBlocks, finallyBlock);
  }

  /**
   * Lazily reconstruct this node.
   * <p> 
   * If any of the children change (upon visitition) construct a new 
   * node and return it. OW, return <code>this</code>.
   *
   * @param tryBlock The Block enclosed under the try
   * @param catchBlocks List of catchblocks (which is a tuple of a
   *  formalparamater
   * and block).
   * @param finallyBlock An optional block for the finally clause
   */
  public TryStatement reconstruct( Node ext, BlockStatement tryBlock, 
                                   List catchBlocks, 
                                   BlockStatement finallyBlock)
  {
    if ( tryBlock != this.tryBlock || this.ext != ext ||
         finallyBlock != this.finallyBlock ||
         catchBlocks.size() != this.catchBlocks.size())
    {
      TryStatement ts = new TryStatement ( ext, tryBlock, catchBlocks, 
                                           finallyBlock) ;
      ts.copyAnnotationsFrom ( this ) ;
      return ts;
    }
    
    for( int i = 0; i < catchBlocks.size(); i++)
    {
      if ( catchBlocks.get( i ) != this.catchBlocks.get( i ) )
      {
        TryStatement ts = new TryStatement ( ext, tryBlock, catchBlocks, 
                                             finallyBlock) ;
        ts.copyAnnotationsFrom ( this ) ;
        return ts;
      }
    }
    
    return this;
  }

  public TryStatement reconstruct( BlockStatement tryBlock, 
                                   List catchBlocks, 
                                   BlockStatement finallyBlock) {
      return reconstruct(this.ext, tryBlock, catchBlocks, finallyBlock);
  }


  /**
   * Effects: Returns the tryBlock for this TryStatement.
   */
  public BlockStatement getTryBlock() {
    return tryBlock;
  }

  /**
   * Effects: Sets the try block for this to be <newTryBlock>.
   */
  public void setTryBlock(BlockStatement newTryBlock) {
    tryBlock = newTryBlock;
  }

  /**
   * Effects: returns the finally block for this TryStatement, or
   * null if there is none.
   */
  public BlockStatement getFinallyBlock() {
    return finallyBlock;
  }

  /**
   * Effects: sets the finally block of this to be <newFinallyBlock>.
   */
  public void setFinallyBlock(BlockStatement newFinallyBlock) {
    finallyBlock = newFinallyBlock;
  }

  /**
   * Effects: returns a TypedListIterator over the CatchBlocks of this.
   */
  public TypedListIterator catchBlocks() {
    return new TypedListIterator(catchBlocks.listIterator(),
				 CatchBlock.class,
				 false);
  }
  
  /**
   *
   */
  public Node visitChildren(NodeVisitor v)
  {
    BlockStatement newTryBlock = (BlockStatement) tryBlock.visit( v);
    List newCatchBlocks = new ArrayList ( catchBlocks.size() );
    
    for (ListIterator it = catchBlocks.listIterator(); it.hasNext(); ) {
      CatchBlock cb =  (CatchBlock)((CatchBlock) it.next()).visit ( v ) ;
      if ( cb != null) 
        newCatchBlocks.add ( cb ) ;
    }
    
    BlockStatement newFinallyBlock = null;
    if ( finallyBlock != null) {
      newFinallyBlock = (BlockStatement) finallyBlock.visit( v);
    }

    return reconstruct ( Node.condVisit(this.ext, v), newTryBlock, newCatchBlocks, newFinallyBlock);
  }

   public Node typeCheck(LocalContext c) 
   {
     // nothing to do. most stuff done in exceptionCheck.
     return this;
   }

  /**
   * Performs exceptionChecking. This is a special method that is called
   * via the exceptionChecker's override method (i.e, doesn't follow the
   * standard model for visitation.  
   *
   * @param ecTryBlock The ExceptionCheckerthat was run against the 
   * child node. It contains
   * the exceptions that can be thrown by the tryBlock
   */
  public Node exceptionCheck(ExceptionChecker ecTryBlock) 
    throws SemanticException
  {
    // first, get exceptions from the try block
    SubtypeSet sThrown = ecTryBlock.getThrowsSet(), 
               sCaught = new SubtypeSet();


    // walk through our catch blocks, making sure that they each can 
    // "catch" something.
    for (Iterator it = catchBlocks.listIterator(); it.hasNext() ; )
    {
      CatchBlock cb = (CatchBlock)it.next();
      TypeSystem typeSystem = cb.getCatchType().getTypeSystem();

      if ( sThrown.remove ( cb.getCatchType() ) ||
           sThrown.contains( cb.getCatchType() ))
      {
        if ( ! sCaught.add ( cb.getCatchType()))
        {
          if ( cb.getCatchType().isUncheckedException() ||
               typeSystem.getException().equals( cb.getCatchType() ) ||
               typeSystem.getThrowable().equals( cb.getCatchType() ))
          {
            // exceptions that don't need to be explicitly declared 
            // ( eg runtime, or java.lang.exception.
            if ( ! sCaught.add ( cb.getCatchType() ) )
              ecTryBlock.reportError( " The exception \"" +
                        cb.getCatchType().getTypeString()+ 
                        "\" has already been caught in this try block.", 
                        Annotate.getLineNumber ( cb )); 
          }
          else
          {
            // this catch block is useless, since no one can throw to it
            ecTryBlock.reportError( "The catch block is unreachable " +
                                    " since no exceptions of type \"" + 
                                    cb.getCatchType().getTypeString() 
                                    + "\" can reach this point.", 
                                    Annotate.getLineNumber ( cb ));
          }
        }
      }
      else
      {
          if ( cb.getCatchType().isUncheckedException() ||
               typeSystem.getException().equals( cb.getCatchType() ) ||
               typeSystem.getThrowable().equals( cb.getCatchType() ))
          {
            // exceptions that don't need to be explicitly declared 
            // ( eg runtime,  or java.lang.exception.
            if ( ! sCaught.add ( cb.getCatchType() ) )
              ecTryBlock.reportError( " The exception \"" +
                    cb.getCatchType().getTypeString() + 
                    "\" has already been caught in this try block.", 
                    Annotate.getLineNumber ( cb )); 
          }
          else
          {
            // this catch block is useless, since no one can throw to it
            ecTryBlock.reportError( "The catch block is unreachable " + 
                                    "since no exceptions of type \"" +
                                    cb.getCatchType().getTypeString() 
                                    + "\" can reach this point.", 
                                    Annotate.getLineNumber ( cb ));
          }
        }
      }
    
    // ecTryBlock now contains any exceptions which 
    // the try block doesn't catch. 
    return this;
  }

   public void  translate(LocalContext c, CodeWriter w)
   {
      w.write("try");
      tryBlock.translate_substmt(c, w);
      
      for (ListIterator it = catchBlocks.listIterator(); it.hasNext(); )
      {
         CatchBlock cb = (CatchBlock) it.next();
	 w.newline(4);
         cb.translate_block(c, w);
      }
      if (finallyBlock != null)
      {
	 w.newline(4);
         w.write ("finally");
         finallyBlock.translate_substmt(c, w);
      }
   }

   public void dump( CodeWriter w)
   {
      w.write( "( TRY ");
      dumpNodeInfo( w);
      w.write( ")");
   }

  private BlockStatement tryBlock;
  private List catchBlocks;
  private BlockStatement finallyBlock;
}

