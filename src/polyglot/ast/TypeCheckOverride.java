package jltools.ast;

import jltools.types.*;
import jltools.visit.*;

public interface TypeCheckOverride
{
    public Node typeCheck(Node n, TypeChecker tc, LocalContext c)
	throws SemanticException;
}
