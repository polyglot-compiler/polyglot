package polyglot.ext.param.types;

import polyglot.ext.jl.types.TypeSystem_c;
import polyglot.types.*;
import polyglot.util.*;

import java.util.*;

/**
 * Type system for parameterized types.
 */
public abstract class ParamTypeSystem_c extends TypeSystem_c
                                     implements ParamTypeSystem
{
    /**
     * Instantiate a parametric type on a list of actual parameters.
     *
     * @param pos The position of the instantiated type
     * @param base The parameterized type
     * @param actuals The list of actuals
     *
     * @throws SemanticException when the actuals do not agree with the formals
     */
    public Type instantiate(Position pos, ParametricType base,
                            List actuals) throws SemanticException
    {
        checkInstantiation(pos, base, actuals);
        return uncheckedInstantiate(pos, base, actuals);
    }

    /**
     * Check that an instantiation of a parametric type on a list of actual
     * parameters is legal.
     *
     * @param pos The position of the instantiated type
     * @param base The parameterized type
     * @param actuals The list of actuals
     *
     * @throws SemanticException when the actuals do not agree with the formals
     */
    protected void checkInstantiation(Position pos, ParametricType base,
                                      List actuals) throws SemanticException
    {
        if (base.formals().size() != actuals.size()) {
            throw new SemanticException("Wrong number of actual parameters " +
                                        "for instantiation of \"" +
                                        base + "\".", pos);
        }
    }

    /**
     * Instantiate a parametric type on a list of actual parameters, but
     * do not check that the instantiation is legal.
     *
     * @param pos The position of the instantiated type
     * @param base The parameterized type
     * @param actuals The list of actuals
     */
    protected Type uncheckedInstantiate(Position pos, ParametricType base,
                                        List actuals)
    {
        Map substMap = new HashMap();
        Iterator i = base.formals().iterator();
        Iterator j = actuals.iterator();

        while (i.hasNext() && j.hasNext()) {
            Object formal = i.next();
            Object actual = j.next();
            substMap.put(formal, actual);
        }

        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("Wrong number of actual " +
                                            "parameters for instantiation " +
                                            "of \"" + base + "\".", pos);
        }

        return subst(base, substMap, new HashMap());
    }

    /**
     * Instantiate a parametric type on its own formals.
     *
     * @param pos The position of the instantiated type
     * @param base The parameterized type
     */
    public Type nullInstantiate(Position pos, ParametricType base) {
        return uncheckedInstantiate(pos, base, base.formals());
    }

    public Type nullInstantiate(ParametricType base) {
        return nullInstantiate(base.position(), base);
    }
    
    /**
     * Apply a parameter substitution to a type.
     *
     * @param t The type on which we perform substitutions.
     * @param substMap Map from formal parameters to actuals; the formals are
     * not necessarily formals of <code>t</code>.
     */
    public Type subst(Type t, Map substMap) {
        return subst(t, substMap, new HashMap());
    }

    /**
     * Apply a parameter substitution to a type.
     *
     * @param t The type on which we perform substitutions.
     * @param substMap Map from formal parameters to actuals; the formals are
     * not necessarily formals of <code>t</code>.
     * @param cache Cache of substitutions performed, implemented as a map from
     * type to substituted type.  This is passed in to ensure pointers to
     * outer classes are substituted correctly.
     */
    public Type subst(Type t, Map substMap, Map cache) {
        return subst(substMap, cache).substType(t);
    }

    /**
     * Create a substitutor.
     *
     * @param substMap Map from formal parameters to actuals; the formals are
     * not necessarily formals of <code>t</code>.
     * @param cache Cache of substitutions performed, implemented as a map from
     * type to substituted type.  This is passed in to ensure pointers to
     * outer classes are substituted correctly.
     */
    public Subst subst(Map substMap, Map cache) {
        return new Subst_c(this, substMap, cache);
    }
}
