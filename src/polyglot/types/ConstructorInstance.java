package polyglot.types;

import java.util.List;

/**
 * A <code>ConstructorInstance</code> contains type information for a
 * constructor.
 */
public interface ConstructorInstance extends ProcedureInstance
{
    /**
     * Set the flags of the constructor.
     */
    ConstructorInstance flags(Flags flags);

    /**
     * Set the types of the formal parameters of the constructor.
     */
    ConstructorInstance argumentTypes(List l);

    /**
     * Set the types of the exceptions thrown by the constructor.
     */
    ConstructorInstance exceptionTypes(List l);

    /**
     * Set the containing class of the constructor.
     */
    ConstructorInstance container(ClassType container);
}
