package polyglot.ext.coffer.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.coffer.types.*;

public interface CanonicalKeySetNode extends KeySetNode
{
    public CanonicalKeySetNode keys(KeySet keys);
}
