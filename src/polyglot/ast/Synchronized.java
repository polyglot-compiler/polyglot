package polyglot.ast;

/**
 * An immutable representation of a Java language <code>synchronized</code>
 * block. Contains an expression being tested and a statement to be executed
 * while the expression is <code>true</code>.
 */
public interface Synchronized extends Stmt
{
    Expr expr();
    Synchronized expr(Expr expr);

    Block body();
    Synchronized body(Block body);
}
