package polyglot.ast;

import java.util.List;

/**
 * A <code>Switch</code> is an immutable representation of a Java
 * <code>switch</code> statement.  Such a statement has an expression which
 * is evaluated to determine where to branch to, an a list of labels
 * and block statements which are conditionally evaluated.  One of the
 * labels, rather than having a constant expression, may be lablled
 * default.
 */
public interface Switch extends Stmt 
{
    Expr expr();
    Switch expr(Expr expr);

    List elements();
    Switch elements(List elements);
}
