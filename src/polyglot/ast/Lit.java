package polyglot.ast;

/**
 * <code>Lit</code> represents any Java literal.
 */
public interface Lit extends Expr
{
    /** Get the value of the literal, as an object. */
    Object objValue();
}
