package polyglot.ast;

import java.util.*;

/**
 * An immutable representation of a Java language <code>for</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public interface For extends Stmt 
{    
    /** List of initialization statements.
     * @return A list of {@link polyglot.ast.ForInit ForInit}.
     */
    List inits();

    /** Set the list of initialization statements.
     * @param inits A list of {@link polyglot.ast.ForInit ForInit}.
     */
    For inits(List inits);

    /** Loop condition */
    Expr cond();

    /** Set the loop condition */
    For cond(Expr cond);

    /** List of iterator expressions.
     * @return A list of {@link polyglot.ast.ForUpdate ForUpdate}.
     */
    List iters();

    /** Set the list of iterator expressions.
     * @param iters A list of {@link polyglot.ast.ForUpdate ForUpdate}.
     */
    For iters(List iters);

    /** Loop body */
    Stmt body();

    /** Set the loop body */
    For body(Stmt body);
}
