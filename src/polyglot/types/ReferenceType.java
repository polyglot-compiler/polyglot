package polyglot.types;

import java.util.List;

/**
 * A <code>ReferenceType</code> represents a reference type: a type which
 * contains methods and fields and which is a subtype of Object.
 */
public interface ReferenceType extends Type
{ 
    Type superType();
    List interfaces();
    List fields();
    List methods();

    FieldInstance fieldNamed(String name);
}
