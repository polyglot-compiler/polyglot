package polyglot.types;

/**
 * An <code>InnerClassType</code> represents any inner class.
 */
public interface InnerClassType extends ClassType
{
    ClassType outer();
}
