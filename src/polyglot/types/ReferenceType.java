package polyglot.types;

import java.util.List;

/**
 * A <code>ReferenceType</code> represents a reference type: a type which
 * contains methods and fields and which is a subtype of Object.
 */
public interface ReferenceType extends Type
{ 
    /**
     * Return the type's super type.
     */
    Type superType();

    /**
     * Return the type's interfaces.
     */
    List interfaces();

    /**
     * Return the type's fields.
     */
    List fields();

    /**
     * Return the type's methods.
     */
    List methods();

    /**
     * Return the field named <code>name</code>, or null.
     */
    FieldInstance fieldNamed(String name);

    /**
     * Return the methods named <code>name</code>, if any.
     */
    List methodsNamed(String name);

    /**
     * Return the methods named <code>name</code> with the given formal
     * parameter types, if any.
     */
    List methods(String name, List argTypes);

    /**
     * Return the true if the type has the given method.
     */
    boolean hasMethod(MethodInstance mi);

    /**
     * Return the true if the type has the given method.
     */
    boolean hasMethodImpl(MethodInstance mi);
}
