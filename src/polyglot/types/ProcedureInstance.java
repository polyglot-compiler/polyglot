package polyglot.types;

import java.util.List;

/**
 * A <code>ProcedureInstance</code> contains the type information for a Java
 * procedure (either a method or a constructor).
 */
public interface ProcedureInstance extends CodeInstance
{
    List argumentTypes();
    List exceptionTypes();

    /**
     * Returns a String representing the signature of the procedure.
     * This includes just the name of the method (or name of the class, if
     * it is a constructor), and the argument types.
     */
    String signature();

    /**
     * Returns either "method" or "constructor"
     */
    String designator();

    boolean moreSpecific(ProcedureInstance pi);
    boolean hasArguments(List arguments);
    boolean throwsSubset(ProcedureInstance pi);
    boolean callValid(List actualTypes);

    boolean moreSpecificImpl(ProcedureInstance pi);
    boolean hasArgumentsImpl(List arguments);
    boolean throwsSubsetImpl(ProcedureInstance pi);
    boolean callValidImpl(List actualTypes);
}
