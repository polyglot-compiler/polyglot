package jltools.ast;

import jltools.types.ConstructorInstance;
import jltools.util.Enum;
import java.util.List;

/**
 * A <code>ConstructorCall</code> is an immutable representation of
 * a direct call to a constructor of a class in the form of
 * <code>super(...)</code>    or <code>this(...)</code>.
 */
public interface ConstructorCall extends Stmt
{
    public static class Kind extends Enum {
        public Kind(String name) { super(name); }
    }

    public static final Kind SUPER = new Kind("super");
    public static final Kind THIS    = new Kind("this");

    Expr qualifier();
    ConstructorCall qualifier(Expr qualifier);

    Kind kind();
    ConstructorCall kind(Kind kind);

    List arguments();
    ConstructorCall arguments(List arguments);

    ConstructorInstance constructorInstance();
}
