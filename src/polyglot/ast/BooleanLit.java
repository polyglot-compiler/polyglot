package polyglot.ast;

/**
 * A <code>BooleanLit</code> represents a boolean literal expression.
 */
public interface BooleanLit extends Lit
{
    boolean value();
    BooleanLit value(boolean value);
}
