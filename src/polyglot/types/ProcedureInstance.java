package jltools.types;

import java.util.List;

/**
 * A <code>ProcedureInstance</code> contains the type information for a Java
 * procedure (either a method or a constructor).
 */
public interface ProcedureInstance extends CodeInstance
{
    List argumentTypes();
    List exceptionTypes();
}
