package polyglot.types;

/**
 * A <code>FieldInstance</code> contains type information for a field.
 */
public interface FieldInstance extends VarInstance, MemberInstance
{
    FieldInstance flags(Flags flags);
    FieldInstance name(String name);
    FieldInstance type(Type type);
    FieldInstance container(ReferenceType container);
    FieldInstance constantValue(Object value);
}
