package jltools.ast;

import jltools.types.Type;
import jltools.util.CodeWriter;

/**
 * An <code>Expr</code> represents any Java expression.  All expressions
 * must be subtypes of Expr.
 */
public interface Expr extends Receiver {
    /**
     * Return the type of this expression, or null if no type has been
     * assigned yet.
     */
    Type type();

    /**
     * Return an equivalent expression, but with the type <code>type</code>.
     */
    Expr type(Type type);

    Precedence precedence();
}
