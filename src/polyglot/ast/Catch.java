package polyglot.ast;

import polyglot.types.Type;
import polyglot.types.SemanticException;

/**
 * A <code>Catch</code> represents one half of a <code>try... catch</code>
 * statement.  Specifically, the second half.
 */
public interface Catch extends Stmt
{
    Type catchType();

    Formal formal();
    Catch formal(Formal formal);

    Block body();
    Catch body(Block body);
}
