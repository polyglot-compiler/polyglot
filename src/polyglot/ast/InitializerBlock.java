/*
 * InitializerBlock.java
 */

package jltools.ast;

import jltools.types.LocalContext;
import jltools.visit.SymbolReader;
import jltools.util.*;

/**
 * Overview: An InitializerBlock is a mutable representation of an
 * initializer block in a Java class (which appears outside of any
 * method).  Such a block is executed before the code for any of the
 * constructors.  Such a block can optionally be static, in which case
 * it is executed when the class is created.  
 */
public class InitializerBlock extends ClassMember {
  /**
   * Effects: Creates a new InitializerBlock containing <block>.
   * The InitializerBlock is static iff <isStatic> is true.
   */
   public InitializerBlock (BlockStatement block,
			    boolean isStatic) {
     this.block = block;
     this.isStatic = isStatic;
   }

  /**
   * Effects: Returns true iff this InitializerBlock contains static code.
   */
  public boolean isStatic() {
    return isStatic;
  }

  /**
   * Effects: Sets this InitializerBlock to contain static code if
   * <isStatic> is true, else the block contains non-static code.
   */
  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  /**
   * Effects: Returns the BlockStatement which comprises this
   * InitializerBlock.
   */
  public BlockStatement getBlock() {
    return block;
  }
  
  /**
   * Effects: Sets this InitializerBlock to contain <newBlock>.
   */
  public void setBlock(BlockStatement newBlock) {
    block = newBlock;
  }


  public void translate(LocalContext c, CodeWriter w)
  {
    w.beginBlock();
    w.write (" { ");
    block.translate(c, w);
    w.write (" } ");
    w.endBlock();
  }

  public Node dump( CodeWriter w)
  {
    w.write( "( INITIALIZER BLOCK ");
    if( isStatic) {
      w.write( "< static > ");
    }
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }
  
  public Node readSymbols( SymbolReader sr)
  {
    return this;
  }

  public Node typeCheck(LocalContext c)
  {
    // FIXME; implement
    return this;
  }

  Object visitChildren(NodeVisitor v) 
  {
    block = (BlockStatement) block.visit(v);
    return v.mergeVisitorInfo( Annotate.getVisitorInfo( this),
                               Annotate.getVisitorInfo( block));
  }

  public Node copy() {
    InitializerBlock ib = new InitializerBlock(block, isStatic);
    ib.copyAnnotationsFrom(this);
    return ib;
  }

  public Node deepCopy() {
    InitializerBlock ib =
      new InitializerBlock((BlockStatement) block.deepCopy(), isStatic);
    ib.copyAnnotationsFrom(this);
    return ib;
  }

  private boolean isStatic;
  private BlockStatement block;
}


  
