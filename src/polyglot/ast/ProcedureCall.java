package polyglot.ast;

import polyglot.types.ProcedureInstance;
import java.util.List;

/**
 * A <code>ProcedureCall</code> is an interface representing a
 * method or constructor call.
 */
public interface ProcedureCall extends Term
{
    /**
     * The call's actual arguments.
     * @return A list of {@link polyglot.ast.Expr Expr}.
     */
    List arguments();

    /**
     * The type object of the method we are calling.  This is, generally, only
     * valid after the type-checking pass.
     */
    ProcedureInstance procedureInstance();
}
