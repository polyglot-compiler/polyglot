package jltools.types;

/**
 * A <code>LocalInstance</code> contains type information for a local variable.
 */
public interface LocalInstance extends VarInstance
{
    LocalInstance flags(Flags flags);
    LocalInstance name(String name);
    LocalInstance type(Type type);
    LocalInstance constantValue(Object value);
    void setType(Type type); //destructive update
}
