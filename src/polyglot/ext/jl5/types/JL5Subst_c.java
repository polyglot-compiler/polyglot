package polyglot.ext.jl5.types;

import java.util.*;

import polyglot.ext.param.types.ParamTypeSystem;
import polyglot.ext.param.types.Subst_c;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

public class JL5Subst_c extends Subst_c implements JL5Subst {

    private final boolean isRawClass;
    public JL5Subst_c(ParamTypeSystem ts, Map subst, Map cache, boolean isRawClass) {
        super(ts, subst, cache);
//        if (subst.isEmpty()) {
//            Thread.dumpStack();
//        }
        for (Iterator i = entries(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            if (e.getKey() instanceof TypeVariable && e.getValue() instanceof Type)
                continue;
            throw new InternalCompilerError("bad map: " + subst);
        }
        
        this.isRawClass = isRawClass;

    }
    
    @Override
    protected Type uncachedSubstType(Type t) {
        if (t instanceof JL5SubstClassType && isRawClass) {
            // don't perform substitution on subst types, just return a raw class
            JL5SubstClassType sct = (JL5SubstClassType)t;
            JL5TypeSystem ts = (JL5TypeSystem)this.typeSystem();
            return ts.rawClass(sct.base(), t.position());
        }
        return super.uncachedSubstType(t);
    }

    @Override
    public Type substType(Type t) {
        if (t instanceof TypeVariable) {
            return substTypeVariable((TypeVariable) t);
        }
        if (t instanceof WildCardType) {
            return substWildCardTypeVariable((WildCardType) t);
        }
        if (t instanceof IntersectionType) {
            return substIntersectionType((IntersectionType)t);
        }
            
        return super.substType(t);
    }


    public Type substTypeVariable(TypeVariable t) {
        if (subst.containsKey(t)) {
            return (Type)subst.get(t);
        }
        for (TypeVariable k : (Set<TypeVariable>)subst.keySet()) {
            if (((TypeVariable_c)k).uniqueIdentifier == ((TypeVariable_c)t).uniqueIdentifier) {
                return (Type) subst.get(k);
            }
        }
        t = (TypeVariable) t.copy();
        t.setUpperBound((ReferenceType)substType(t.upperBound()));
        return t;
    }

    public Type substWildCardTypeVariable(WildCardType t) {
        WildCardType n = t;
        n = n.upperBound((ReferenceType) substType(t.upperBound()));
        n = n.lowerBound((ReferenceType) substType(t.lowerBound()));
        return n;
    }
    
    public Type substIntersectionType(IntersectionType t) {
        List<ReferenceType> s = this.substTypeList(t.bounds());
        t = (IntersectionType)t.copy();
        t.setBounds(s);
        return t;
    }


    @Override
    protected Object substSubstValue(Object value) {
        return substType((Type) value);
    }

    @Override
    public ClassType substClassType(ClassType t) {
        // Don't bother trying to substitute into a non-JL5 class.
        if (! (t instanceof JL5ClassType)) {
            return t;
        }

        if (t instanceof RawClass) {
            // don't substitute raw classes
            return t;
        }
        if (t instanceof JL5SubstClassType) {
            // this case should be impossible
            throw new InternalCompilerError("Should have no JL5SubstClassTypes");
        }
        
        if (t instanceof JL5ParsedClassType) {
            JL5ParsedClassType pct = (JL5ParsedClassType)t;            
            if (pct.typeVariables().isEmpty()) {
                // no parameters to be instantiated!
                return pct;
            }
            if (isRawClass) {
                return ((JL5TypeSystem) ts).rawClass(pct, t.position());
            }
            return new JL5SubstClassType_c((JL5TypeSystem) ts, t.position(),
                                           (JL5ParsedClassType) t, this);
        }

        throw new InternalCompilerError("Don't know how to handle class type " + t.getClass());

    }

    @Override
    public MethodInstance substMethod(MethodInstance mi) {
        JL5MethodInstance mj = (JL5MethodInstance) super.substMethod(mi);
        if (mj.typeParams() != null && !mj.typeParams().isEmpty()) {
            // remove any type params we have replced.
            List l = new ArrayList(mj.typeParams());
            l.removeAll(this.subst.keySet());
            mj = (JL5MethodInstance) mj.copy();
            mj.setTypeParams(l);
        }
        return mj;
    }

    @Override
    public ConstructorInstance substConstructor(ConstructorInstance ci) {
        JL5ConstructorInstance cj = (JL5ConstructorInstance) super.substConstructor(ci);
        if (cj.typeParams() != null && !cj.typeParams().isEmpty()) {
            // remove any type params we have replced.
            List l = new ArrayList(cj.typeParams());
            l.removeAll(this.subst.keySet());
            cj = (JL5ConstructorInstance) cj.copy();
            cj.setTypeParams(l);
        }
        return cj;
    }

    @Override
    public JL5ProcedureInstance substProcedure(JL5ProcedureInstance mi) {        
        if (mi instanceof MethodInstance) {
            return (JL5ProcedureInstance) substMethod((MethodInstance) mi);
        }
        else {
            return (JL5ProcedureInstance) substConstructor((ConstructorInstance) mi);
        }
    }
}
