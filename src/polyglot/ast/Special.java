package polyglot.ast;

import polyglot.util.Enum;

/**
 * A <code>Special</code> is an immutable representation of a
 * reference to <code>this</code> or <code>super</code in Java.  This
 * reference can be optionally qualified with a type such as 
 * <code>Foo.this</code>.
 */
public interface Special extends Expr 
{    
    /** Special expression kind: either "super" or "this". */
    public static class Kind extends Enum {
        public Kind(String name) { super(name); }
    }

    public static final Kind SUPER = new Kind("super");
    public static final Kind THIS  = new Kind("this");

    Kind kind();
    Special kind(Kind kind);

    TypeNode qualifier();
    Special qualifier(TypeNode qualifier);
}
