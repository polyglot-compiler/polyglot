package jltools.ast;

import jltools.util.Enum;

/** 
 * A <code>FloatLit</code> represents a literal in java of type
 * <code>float</code> or <code>double</code>.
 */
public interface FloatLit extends Lit 
{    
    /** Floating point literal kind: either float or double. */
    public static class Kind extends Enum {
        Kind(String name) { super(name); }
    }

    public static final Kind FLOAT = new Kind("float");
    public static final Kind DOUBLE = new Kind("dounle");

    Kind kind();
    FloatLit kind(Kind kind);

    double value();
    FloatLit value(double value);
}
