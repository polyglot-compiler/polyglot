package jltools.ast;

/**
 * A <code>Switch</code> is an immutable representation of a Java
 * <code>switch</code> statement.  Such a statement has an expression which
 * is evaluated to determine where to branch to, an a list of labels
 * and block statements which are conditionally evaluated.  One of the
 * labels, rather than having a constant expression, may be labeled
 * default.
 */
public interface Case extends SwitchElement
{
    Expr expr();
    Case expr(Expr expr);

    boolean isDefault();
    long value();
}
