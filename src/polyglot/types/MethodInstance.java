package polyglot.types;

import java.util.List;

/**
 * A <code>MethodInstance</code> represents the type information for a Java
 * method.
 */
public interface MethodInstance extends ProcedureInstance
{
    Type returnType();
    MethodInstance returnType(Type returnType);

    String name();
    MethodInstance name(String name);

    MethodInstance flags(Flags flags);
    MethodInstance argumentTypes(List l);
    MethodInstance exceptionTypes(List l);
    MethodInstance container(ReferenceType container);

    List overrides(TypeSystem ts);
    boolean canOverride(TypeSystem ts, MethodInstance mi);
    boolean isSameMethod(TypeSystem ts, MethodInstance mi);
    boolean methodCallValid(TypeSystem ts, MethodInstance call);
    boolean methodCallValid(TypeSystem ts, String name, List actualTypes);
}
