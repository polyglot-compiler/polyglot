package polyglot.types;

/**
 * A <code>InitializerInstance</code> contains the type information for a
 * static or anonymous initializer.
 */
public interface InitializerInstance extends CodeInstance
{
    InitializerInstance flags(Flags flags);
    InitializerInstance container(ClassType container);
}
