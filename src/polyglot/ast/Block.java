package polyglot.ast;

import java.util.List;

/**
 * A <code>Block</code> represents a Java block statement -- an immutable
 * sequence of statements.
 */
public interface Block extends Stmt
{
    /**
     * Statements in the block.
     * A list of <code>Stmt</code>.
     * @see polyglot.ast.Stmt
     */
    List statements();

    /**
     * Set the statements in the block.
     * A list of <code>Stmt</code>.
     * @see polyglot.ast.Stmt
     */
    Block statements(List statements);

    /**
     * Append a statement to the block, returning a new block.
     */
    Block append(Stmt stmt);

    /**
     * Prepend a statement to the block, returning a new block.
     */
    Block prepend(Stmt stmt);
}
