package polyglot.ast;

import polyglot.types.FieldInstance;

/**
 * A <code>Field</code> is an immutable representation of a Java field
 * access.  It consists of field name and may also have either a 
 * <code>Type</code> or an <code>Expr</code> containing the field being 
 * accessed.
 */
public interface Field extends Expr
{
    /**
     * Get the type object for the field.  This field may not be valid until
     * after type checking.
     */
    FieldInstance fieldInstance();

    /** Set the type object for the field. */
    Field fieldInstance(FieldInstance fi);

    /**
     * Get the field's container object or type.  May be null before
     * disambiguation.
     */
    Receiver target();

    /** Set the field's container object or type. */
    Field target(Receiver target);

    /** Get the field's name. */
    String name();
    /** Set the field's name. */
    Field name(String name);
}
