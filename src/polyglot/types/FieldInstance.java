package polyglot.types;

/**
 * A <code>FieldInstance</code> contains type information for a field.
 */
public interface FieldInstance extends VarInstance, MemberInstance
{
    FieldInstance container(ReferenceType container);
    FieldInstance flags(Flags flags);
    FieldInstance name(String name);
    FieldInstance type(Type type);   
    FieldInstance constantValue(Object value);
    FieldInstance notConstant();
    FieldInstance orig();
}
