package polyglot.ast;

import polyglot.types.*;
import java.util.List;

/**
 * A <code>Thrower</code> is any computation that might throw an exception.
 */
public interface Thrower extends Computation
{
    /** List of Types of exceptions that might get thrown.  The result is
     * not necessarily correct until after type checking. */
    List throwTypes(TypeSystem ts);
}
