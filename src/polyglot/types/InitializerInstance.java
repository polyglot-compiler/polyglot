package jltools.types;

/**
 * A <code>InitializerInstance</code> contains the type information for a Java
 * procedure (either a method or a constructor).
 */
public interface InitializerInstance extends CodeInstance
{
    InitializerInstance flags(Flags flags);
    InitializerInstance container(ClassType container);
}
