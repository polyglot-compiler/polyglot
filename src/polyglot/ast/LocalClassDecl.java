package jltools.ast;

import jltools.util.*;
import jltools.types.*;

/**
 * A local class declaration statement.  The node is just a wrapper around
 * a class declaration.
 */
public interface LocalClassDecl extends Stmt
{
    ClassDecl decl();
}
