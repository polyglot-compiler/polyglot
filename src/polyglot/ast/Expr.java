package jltools.ast;

import jltools.types.Type;
import jltools.util.CodeWriter;

/*
 * An <code>Expr</code> represents any Java expression.  All expressions
 * must be subtypes of Expr.
 */
public interface Expr extends Receiver {
    Type type();
    Expr type(Type type);

    Precedence precedence();
}
