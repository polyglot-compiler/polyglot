package polyglot.ast;

import java.util.List;

/**
 * An immutable representation of a <code>try</code> block, one or more
 * <code>catch</code> blocks, and an optional <code>finally</code> block.
 */
public interface Try extends Stmt
{
    /** The block to "try". */
    Block tryBlock();

    /** Set the block to "try". */
    Try tryBlock(Block tryBlock);

    /** List of catch blocks. */
    List catchBlocks();

    /** Set the list of catch blocks. */
    Try catchBlocks(List catchBlocks);

    /** The block to "finally" execute. */
    Block finallyBlock();

    /** Set the block to "finally" execute. */
    Try finallyBlock(Block finallyBlock);
}
