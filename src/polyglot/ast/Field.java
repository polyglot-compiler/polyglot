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
    FieldInstance fieldInstance();
    Field fieldInstance(FieldInstance fi);

    Receiver target();
    Field target(Receiver target);

    String name();
    Field name(String name);
}
