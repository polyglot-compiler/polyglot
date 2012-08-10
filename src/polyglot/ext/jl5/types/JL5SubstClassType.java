package polyglot.ext.jl5.types;

import polyglot.util.CodeWriter;

/**
 * A JL5SubstClassType represents a class that has instantiated parameters, or a class that has
 * an enclosing class that has instantiated parameters.
 *
 */
public interface JL5SubstClassType extends JL5SubstType {
    /** The type on which substitutions are performed. */
    @Override
    JL5ParsedClassType base();

    /**
     * Print just the params
     */
    void printParams(CodeWriter w);

}
