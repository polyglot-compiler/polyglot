package jltools.ast;

import jltools.types.ConstructorInstance;
import jltools.types.ParsedAnonClassType;
import java.util.List;

/**
 * A <code>New</code> is an immutable representation of the use of the
 * <code>new</code> operator to create a new instance of a class.  In
 * addition to the type of the class being created, a <code>New</code> has a
 * list of arguments to be passed to the constructor of the object and an
 * optional <code>ClassBody</code> used to support anonymous classes.
 */
public interface New extends Expr
{
    ParsedAnonClassType anonType();
    New anonType(ParsedAnonClassType anonType);

    ConstructorInstance constructorInstance();
    New constructorInstance(ConstructorInstance ci);

    Expr qualifier();
    New qualifier(Expr qualifier);

    TypeNode objectType();
    New objectType(TypeNode t);

    List arguments();
    New arguments(List a);

    ClassBody body();
    New body(ClassBody b);
}
