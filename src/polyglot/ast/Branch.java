package jltools.ast;

import jltools.util.Enum;

/**
 * A <code>Branch</code> is an immutable representation of a branch
 * statment in Java (a break or continue).
 */
public interface Branch extends Stmt
{
    /** Branch kind: either break or continue. */
    public static class Kind extends Enum {
        Kind(String name) { super(name); }
    }

    public static final Kind BREAK    = new Kind("break");
    public static final Kind CONTINUE = new Kind("continue");

    Kind kind();
    Branch kind(Kind kind);

    String label();
    Branch label(String label);
}
