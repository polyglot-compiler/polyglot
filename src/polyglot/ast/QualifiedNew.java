package jltools.ast;

import jltools.types.ConstructorInstance;
import java.util.List;

/**
 * A <code>QualifiedNew</code> is an immutable representation of the use of the
 * qualified <code>new</code> operator to create a new instance of a member
 * class.  In addition to the type of the class being created, a
 * <code>QualifiedNew</code> has a list of arguments to be passed to the
 * constructor of the object and an optional <code>ClassBody</code> used to
 * support anonymous classes.  It is also preceded by an qualifier expression
 * which specifies the context in which the object is being created.
 */
public interface QualifiedNew extends Expr 
{
    Expr qualifier();
    QualifiedNew qualifier(Expr qualifier);

    TypeNode objectType();
    QualifiedNew objectType(TypeNode objectType);

    List arguments();
    QualifiedNew arguments(List arguments);

    ClassBody body();
    QualifiedNew body(ClassBody body);
}
