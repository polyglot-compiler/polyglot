package jltools.ast;

import jltools.types.*;
import jltools.visit.*;

public interface CleanupSignaturesOverride
{
    public Node cleanupSignatures(Node n, SignatureCleaner sc, LocalContext c)
	throws SemanticException;
}
