package jltools.types;

/**
 * A <code>VarInstance</code> contains type information for a variable.  It may
 * be either a local or a field.
 */
public interface VarInstance extends TypeObject
{
    Flags flags();
    String name();
    Type type();
    Object constantValue();
    boolean isConstant();
    void setType(Type type); //destructive update    
}
