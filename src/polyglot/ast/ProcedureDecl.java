package jltools.ast;

import jltools.types.ProcedureInstance;
import jltools.types.Flags;
import java.util.List;

/**
 * A method declaration.
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
