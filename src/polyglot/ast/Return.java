package polyglot.ast;

/**
 * A <code>Return</code> represents a <code>return</code> statement in Java.
 * It may or may not return a value.  If not <code>expr()</code> should return
 * null.
 */
public interface Return extends Stmt
{
    Expr expr();
    Return expr(Expr expr);
}
