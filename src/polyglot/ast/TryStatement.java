/*
 * TryStatement.java
 */

package jltools.ast;

import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Overview: A mutable representation of a try block, one or more
 * catch blocks, and an optional finally block.
 */
public class TryStatement extends Statement {
    
  /**
   * Requires: <catchBlocks> contains only elements of
   * type CatchStatement
   *
   * Effects: Creates a new TryStatement with <tryBlock> as the try
   * block, the catch blocks in <catchBlocks> and a fiannly block of
   * <finallyBlock>.  If there is no finally block, then
   * <finallyBlock> should be null.
   */
  public TryStatement(BlockStatement tryBlock,
		      List catchBlocks,
		      BlockStatement finallyBlock) {
    this.tryBlock = tryBlock;
    this.finallyBlock = finallyBlock;
    TypedList.check(catchBlocks, CatchBlock.class);
    this.catchBlocks = new ArrayList(catchBlocks);
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
  
  public Node accept(NodeVisitor v) {
    return v.visitTryStatement(this);
  }

  public void visitChildren(NodeVisitor v) {
    tryBlock = (BlockStatement) tryBlock.accept(v);
    for (ListIterator iter = catchBlocks.listIterator(); it.hasNext(); ) {
      CatchBlock cb = (CatchBlock) it.next();
      it.set((CatchBlock) cb.accept(v));
    }
    finallyBlock = (BlockStatement) finallyBlock.accept(v);
  }

  public Node copy() {
    TryStatement ts = new TryStatement(tryBlock,
				       new ArrayList(catchBlocks),
				       finallyBlock);
    ts.copyAnnotationsFrom(this);
    return ts;
  }

  public Node deepCopy() {
    List newCatchBlocks = new ArrayList(catchBlocks.size());
    for(Iterator it = catchBlocks.iterator(); it.hasNext(); ) {
      CatchBlock cb = (CatchBlock) it.next();
      newCatchBlocks.add(cb.deepCopy());
    }
    TryStatement ts = 
      new TryStatement((BlockStatement) tryBlock.deepCopy(),
		       newCatchBlocks,
		       (BlockStatement) finallyBlock.deepCopy());
    ts.copyAnnotationsFrom(this);
    return ts;
  }

  private BlockStatement tryBlock;
  private List catchBlocks;
  private BlockStatement finallyBlock;
}

