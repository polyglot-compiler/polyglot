package jltools.ast;

import java.util.List;

/**
 * An immutable representation of a <code>try</code> block, one or more
 * <code>catch</code> blocks, and an optional <code>finally</code> block.
 */
public interface Try extends Stmt
{
    Block tryBlock();
    Try tryBlock(Block tryBlock);

    List catchBlocks();
    Try catchBlocks(List catchBlocks);

    Block finallyBlock();
    Try finallyBlock(Block finallyBlock);
}
