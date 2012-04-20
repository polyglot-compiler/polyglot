package polyglot.ext.jl5.types;

import polyglot.util.CodeWriter;


public interface JL5SubstClassType extends JL5SubstType {
    /** The type on which substitutions are performed. */ 
    @Override
    JL5ParsedClassType base();

    /**
     * Print just the params
     */
    void printParams(CodeWriter w);

}
