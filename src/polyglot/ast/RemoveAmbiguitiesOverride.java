package jltools.ast;

import jltools.types.*;
import jltools.visit.*;

public interface RemoveAmbiguitiesOverride
{
    public Node removeAmbiguities(Node n, AmbiguityRemover ar, LocalContext c)
	throws SemanticException;
}
