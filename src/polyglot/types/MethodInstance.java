package polyglot.types;

import java.util.List;

/**
 * A <code>MethodInstance</code> represents the type information for a Java
 * method.
 */
public interface MethodInstance extends ProcedureInstance
{
    /**
     * The method's return type.
     */
    Type returnType();

    /**
     * Set the method's return type.
     */
    MethodInstance returnType(Type returnType);

    /**
     * The method's name.
     */
    String name();

    /**
     * Set the method's name.
     */
    MethodInstance name(String name);

    /**
     * Set the method's flags.
     */
    MethodInstance flags(Flags flags);

    /**
     * Set the method's formal parameter types.
     */
    MethodInstance argumentTypes(List l);

    /**
     * Set the method's exception throw types.
     */
    MethodInstance exceptionTypes(List l);

    /**
     * Set the method's containing type.
     */
    MethodInstance container(ReferenceType container);

    /**
     * Get the list of methods this method (potentially) overrides, in order
     * from this class (i.e., including <code>this</code>) to super classes.
     */
    List overrides();

    /**
     * Return true if this method can override <code>mi</code>.
     */
    boolean canOverride(MethodInstance mi);

    /**
     * Return true if this method has the same signature as <code>mi</code>.
     */
    boolean isSameMethod(MethodInstance mi);

    /**
     * Return true if this method can be called with name <code>name</code>
     * and actual parameters of types <code>actualTypes</code>.
     */
    boolean methodCallValid(String name, List actualTypes);

    /**
     * Get the list of methods this method (potentially) overrides, in order
     * from this class (i.e., including <code>this</code>) to super classes.
     * This method should not be called except by <code>TypeSystem</code>
     * and by subclasses.
     */
    List overridesImpl();

    /**
     * Return true if this method can override <code>mi</code>.
     * This method should not be called except by <code>TypeSystem</code>
     * and by subclasses.
     */
    boolean canOverrideImpl(MethodInstance mi);

    /**
     * Return true if this method has the same signature as <code>mi</code>.
     * This method should not be called except by <code>TypeSystem</code>
     * and by subclasses.
     */
    boolean isSameMethodImpl(MethodInstance mi);

    /**
     * Return true if this method can be called with name <code>name</code>
     * and actual parameters of types <code>actualTypes</code>.
     * This method should not be called except by <code>TypeSystem</code>
     * and by subclasses.
     */
    boolean methodCallValidImpl(String name, List actualTypes);
}
