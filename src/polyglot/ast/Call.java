package jltools.ast;

import jltools.types.MethodInstance;
import java.util.List;

/**
 * A <code>Call</code> is an immutable representation of a Java
 * method call.  It consists of a method name and a list of arguments.
 * It may also have either a Type upon which the method is being
 * called or an expression upon which the method is being called.
 */
public interface Call extends Expr
{
    Receiver target();
    Call target(Receiver target);

    String name();
    Call name(String name);

    List arguments();
    Call arguments(List arguments);

    MethodInstance methodInstance();
    Call methodInstance(MethodInstance mi);
}
