package polyglot.types;

import java.util.List;

/**
 * A <code>ConstructorInstance</code> contains type information for a
 * constructor.
 */
public interface ConstructorInstance extends ProcedureInstance
{
    /** Non-destructive updates. */
    ConstructorInstance flags(Flags flags);
    ConstructorInstance formalTypes(List l);
    ConstructorInstance throwTypes(List l);
    ConstructorInstance container(ClassType container);
}
