package polyglot.ast;

import polyglot.util.Enum;

/**
 * An <code>Import</code> is an immutable representation of a Java
 * <code>import</code> statement.  It consists of the string representing the
 * item being imported and the kind which is either indicating that a class
 * is being imported, or that an entire package is being imported.
 */
public interface Import extends Node 
{
    /** Import kinds: class (e.g., import java.util.Set) or package (e.g.,
     *  import java.util.*). */
    public static class Kind extends Enum {
        public Kind(String name) { super(name); }
    }

    public static final Kind CLASS   = new Kind("class");
    public static final Kind PACKAGE = new Kind("package");

    /** Get the name of the class or package to import. */
    String name();
    /** Set the name of the class or package to import. */
    Import name(String name);

    /** Get the kind of import. */
    Kind kind();
    /** Set the kind of import. */
    Import kind(Kind kind);
}
