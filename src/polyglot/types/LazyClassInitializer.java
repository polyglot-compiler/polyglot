package polyglot.types;

/**
 * A LazyClassInitializer is responsible for initializing members of
 * a class after it has been created.  Members are initialized lazily
 * to correctly handle cyclic dependencies between classes.
 */
public interface LazyClassInitializer
{
    public boolean fromClassFile();
    public void initConstructors(ParsedClassType ct);
    public void initMethods(ParsedClassType ct);
    public void initFields(ParsedClassType ct);
    public void initMemberClasses(ParsedClassType ct);
    public void initInterfaces(ParsedClassType ct);
}
