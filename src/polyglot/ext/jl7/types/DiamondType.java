package polyglot.ext.jl7.types;

import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;

public interface DiamondType extends JL5ClassType {
    /**
     * The JL5ParsedClassType of which this is the diamond version. This 
     * JL5ParsedClassType will have at least one type parameter.
     * 
     */
    JL5ParsedClassType base();

    /**
     * Return the JL5SubstClassType inferred version of the diamond class.
     */
    JL5SubstClassType inferred();

    /**
     * Set the inferred JL5SubstClassType asoociated with this diamond class.
     */
    void inferred(JL5SubstClassType inferred);
}
