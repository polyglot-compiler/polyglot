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
    /** The constructor's flags. */
    Flags flags();

    /** Set the constructor's flags. */
    ConstructorDecl flags(Flags flags);

    /**
     * The constructor's name.  This should be the short name of the
     * containing class.
     */
    String name();

    /** Set the constructor's name. */
    ConstructorDecl name(String name);

    /** The constructor's formal parameters. */
    List formals();

    /** Set the constructor's formal parameters. */
    ConstructorDecl formals(List formals);

    /** The constructor's exception throw types. */
    List exceptionTypes();

    /** Set the constructor's exception throw types. */
    ConstructorDecl exceptionTypes(List exceptionTypes);

    /** The constructor's body. */
    Block body();

    /** Set the constructor's body. */
    ConstructorDecl body(Block body);

    /**
     * The constructor type object.  This field may not be valid until
     * after type checking.
     */
    ConstructorInstance constructorInstance();

    /** Set the constructor's type object. */
    ConstructorDecl constructorInstance(ConstructorInstance ci);
}
