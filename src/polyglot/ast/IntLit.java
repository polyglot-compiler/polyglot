package jltools.ast;

import jltools.util.Enum;

/** 
 * An <code>IntLit</code> represents a literal in Java of an integer
 * type.
 */
public interface IntLit extends NumLit 
{
    public static class Kind extends Enum {
        protected Kind(String name) { super(name); }
    }

    public static final Kind BYTE  = new Kind("byte");
    public static final Kind SHORT = new Kind("short");
    public static final Kind INT   = new Kind("int");
    public static final Kind LONG  = new Kind("long");

    long value();
    IntLit value(long value);

    Kind kind();
    IntLit kind(Kind kind);
}
