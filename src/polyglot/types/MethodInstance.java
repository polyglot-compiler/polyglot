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

    List overrides();
    boolean canOverride(MethodInstance mi);
    boolean isSameMethod(MethodInstance mi);
    boolean methodCallValid(String name, List actualTypes);

    List overridesImpl();
    boolean canOverrideImpl(MethodInstance mi);
    boolean isSameMethodImpl(MethodInstance mi);
    boolean methodCallValidImpl(String name, List actualTypes);
}
