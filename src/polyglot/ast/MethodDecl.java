package polyglot.ast;

import polyglot.types.MethodInstance;
import polyglot.types.Flags;
import java.util.List;

/**
 * A method declaration.
 */
public interface MethodDecl extends ProcedureDecl 
{
    Flags flags();
    MethodDecl flags(Flags flags);

    TypeNode returnType();
    MethodDecl returnType(TypeNode returnType);

    String name();
    MethodDecl name(String name);

    List formals();
    MethodDecl formals(List formals);

    List exceptionTypes();
    MethodDecl exceptionTypes(List exceptionTypes);

    Block body();
    MethodDecl body(Block body);

    MethodInstance methodInstance();
    MethodDecl methodInstance(MethodInstance mi);
}
