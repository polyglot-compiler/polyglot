package jltools.ast;

import jltools.types.ConstructorInstance;
import java.util.List;

/**
 * A <code>New</code> is an immutable representation of the use of the
 * <code>new</code> operator to create a new instance of a class.  In
 * addition to the type of the class being created, a <code>New</code> has a
 * list of arguments to be passed to the constructor of the object and an
 * optional <code>ClassBody</code> used to support anonymous classes. Such an
 * expression may also be proceeded by an qualifier expression which specifies
 * the context in which the object is being created.
 */
public interface New extends Expr 
{
    TypeNode objectType();
    New objectType(TypeNode t);

    List arguments();
    New arguments(List a);

    ClassBody body();
    New body(ClassBody b);

    ConstructorInstance constructorInstance();
}
