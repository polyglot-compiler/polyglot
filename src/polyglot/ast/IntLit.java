package polyglot.ast;

import polyglot.util.Enum;

/** 
 * An <code>IntLit</code> represents a literal in Java of an integer
 * type.
 */
public interface IntLit extends NumLit 
{
    /** Integer literal kinds: byte, short, int, or long. */
    public static class Kind extends Enum {
        protected Kind(String name) { super(name); }
    }

    public static final Kind BYTE  = new Kind("byte");
    public static final Kind SHORT = new Kind("short");
    public static final Kind INT   = new Kind("int");
    public static final Kind LONG  = new Kind("long");

    /** Get the literal's value. */
    long value();
    /** Set the literal's value. */
    IntLit value(long value);

    /** Get the kind of the literal. */
    Kind kind();
    /** Set the kind of the literal. */
    IntLit kind(Kind kind);
}
