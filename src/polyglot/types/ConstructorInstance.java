package jltools.types;

import java.util.List;

/**
 * A <code>ConstructorInstance</code> contains type information for a
 * constructor.
 */
public interface ConstructorInstance extends ProcedureInstance
{
    ConstructorInstance flags(Flags flags);
    ConstructorInstance argumentTypes(List l);
    ConstructorInstance exceptionTypes(List l);
    ConstructorInstance container(ClassType container);
}
