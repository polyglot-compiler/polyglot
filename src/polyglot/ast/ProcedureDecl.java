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
    Flags flags();
    String name();
    List formals();
    List exceptionTypes();
    Block body();
    ProcedureInstance procedureInstance();
}
