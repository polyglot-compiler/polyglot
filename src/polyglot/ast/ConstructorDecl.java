package polyglot.ast;

import polyglot.types.ConstructorInstance;
import java.util.List;
import polyglot.types.Flags;

/**
 * A <code>ConstructorDecl</code> is an immutable representation of a
 * constructor declaration as part of a class body. 
 */
public interface ConstructorDecl extends ProcedureDecl 
{
    Flags flags();
    ConstructorDecl flags(Flags flags);

    String name();
    ConstructorDecl name(String name);

    List formals();
    ConstructorDecl formals(List formals);

    List exceptionTypes();
    ConstructorDecl exceptionTypes(List exceptionTypes);

    Block body();
    ConstructorDecl body(Block body);

    ConstructorInstance constructorInstance();
    ConstructorDecl constructorInstance(ConstructorInstance ci);
}
