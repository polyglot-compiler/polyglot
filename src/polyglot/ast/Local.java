package jltools.ast;

import jltools.types.LocalInstance;

/** 
 * A local variable expression.
 */
public interface Local extends Expr 
{
    String name();
    Local name(String name);

    LocalInstance localInstance();
    Local localInstance(LocalInstance li);
}
