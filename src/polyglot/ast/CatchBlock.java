/*
 * CatchBlock.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.Context;

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

  public void translate ( Context c, CodeWriter w)
  {
    w.write ( " catch ( " );
    formalParameter.translate( c, w);
    w.write ( " )");
    w.beginBlock();
    block.translate (c, w);
    w.endBlock();
  }
  
  public void dump (Context c, CodeWriter w)
  {
    w.write (" (  CATCH BLOCK " ) ;
    formalParameter.dump( c, w);
    w.beginBlock();
    block.dump(c, w);
    w.endBlock();
  }

  public Node typeCheck(Context c)
  {
    // FIXME: implement;
    return this;
  }


  public void visitChildren(NodeVisitor v) {
    formalParameter.setType((TypeNode) formalParameter.getType().visit(v));
    block = (BlockStatement) block.visit(v);
  }

  public Node copy() {
    CatchBlock cb = new CatchBlock(formalParameter, block);
    cb.copyAnnotationsFrom(this);
    return cb;
  }

  public Node deepCopy() {
    CatchBlock cb = 
      new CatchBlock((FormalParameter)formalParameter.deepCopy(), 
		     (BlockStatement) block.deepCopy());
    cb.copyAnnotationsFrom(this);
    return cb;
  }

  private FormalParameter formalParameter;
  private BlockStatement block;
}
