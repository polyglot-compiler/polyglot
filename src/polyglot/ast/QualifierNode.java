package jltools.ast;

import jltools.types.TypeSystem;
import jltools.types.Qualifier;
import jltools.types.SemanticException;

/**
 * A <code>QualifierNode</code> represents any node that can be used as a type
 * qualifier (<code>jltools.types.Qualifier</code>).  It can resolve to either
 * an enclosing type or can be a package.
 */
public interface QualifierNode extends Prefix
{
    Qualifier qualifier();
}
