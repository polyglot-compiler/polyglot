package polyglot.ext.param.types;

import polyglot.types.*;
import polyglot.util.*;

import polyglot.ext.jl.types.*;

import java.util.*;

/**
 * A base implementation for parametric mutable class types
 * that implements the ParametricType interface.
 */
public class ParametricParsedClassType_c extends ParsedClassType_c 
    implements ParametricParsedClassType 
{
    List/*[Param]*/ formals;
	
    protected ParametricParsedClassType_c() { }

    public ParametricParsedClassType_c(TypeSystem ts, 
	LazyClassInitializer init) 
    {
	super(ts, init);
	formals = new TypedList(new LinkedList(), Param.class, false);
    }

    public List formals() {
	return formals;
    }

    public Type instantiate(Position pos, List actuals) 
	throws SemanticException
    {
	ParamTypeSystem pts = (ParamTypeSystem) typeSystem();
        return pts.instantiate(pos, this, actuals);
    }

    public Type nullInstantiate(Position pos) {
	ParamTypeSystem pts = (ParamTypeSystem) typeSystem();
        return pts.nullInstantiate(pos, this);
    }
}
