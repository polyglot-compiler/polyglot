package polyglot.ast;

import polyglot.types.ProcedureInstance;
import polyglot.types.Flags;
import java.util.List;

/**
 * A procedure declaration.  A procedure is the supertype of methods and
 * constructors.
 */
public interface ProcedureDecl extends ClassMember 
{
    /** The procedure's flags. */
    Flags flags();

    /** The procedure's name. */
    String name();

    /** The procedure's formal parameters.
     * A list of <code>Formal</code>.
     * @see polyglot.ast.Formal
     */
    List formals();

    /** The procedure's exception throw types.
     * A list of <code>TypeNode</code>.
     * @see polyglot.ast.TypeNode
     */
    List exceptionTypes();

    /** The procedure's body. */
    Block body();

    /**
     * The procedure type object.  This field may not be valid until
     * after signature disambiguation.
     */
    ProcedureInstance procedureInstance();
}
