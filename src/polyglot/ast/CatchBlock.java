/*
 * CatchBlock.java
 */

package jltools.ast;

/**
 * Overview: Represents a mutable pair of BlockStatements and
 * FormalParameters which represent a catch block.
 */

public class CatchBlock extends Node {
  
  /**
   * Effects: creates a new CatchBlock with FormalParameter
   * <formalParameter> and BlockStatement <block>.
   */
  public CatchBlock(FormalParameter formalParameter,
		    BlockStatement block) {
    this.formalParameter = formalParameter;
    this.block = block;
  }

  /**
   * Effects: returns the FormalParameter associated with this
   * CatchBlock.
   */
  public FormalParameter getFormalParameter() {
    return formalParameter;
  }

  /**
   * Effects: sets the FormalParameter associated with this CatchBlock
   * to <newParam>.
   */
  public void setFormalParameter(FormalParameter newParam) {
    formalParameter = newParam;
  }

  /**
   * Effects: returns the BlockStatement for this.
   */
  public BlockStatement getBlockStatement() {
    return block;
  }

  /**
   * Effects: sets the block statement for this to be <newBlock>.
   */
  public void setBlockStatement(BlockStatement newBlock) {
    block = newBlock;
  }

  public Node accept(NodeVisitor v) {
    return v.visitCatchBlock(this);
  }

  public void visitChildren(NodeVisitor v) {
    formalParameter.setType((TypeNode) formalParameter.getType().accept(v));
    block = (BlockStatement) block.accept(v);
  }

  public Node copy() {
    CatchBlock cb = new CatchBlock(formalParameter, block);
    cb.copyAnnotationsFrom(this);
    return cb;
  }

  public Node deepCopy() {
    CatchBlock cb = 
      new CatchBlock(formalParameter.deepCopy(), 
		     (BlockStatement) block.deepCopy());
    cb.copyAnnotationsFrom(this);
    return cb;
  }

  private FormalParameter formalParameter;
  private BlockStatement block;
}
