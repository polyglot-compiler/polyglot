package polyglot.ast;

import java.util.List;

/**
 * A <code>Block</code> represents a Java block statement -- an immutable
 * sequence of statements.
 */
public interface Block extends Stmt
{
    List statements();
    Block statements(List statements);
    Block append(Stmt stmt);
    Block prepend(Stmt stmt);
}
