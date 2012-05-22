package polyglot.ext.jl5.types;

import java.util.*;

import polyglot.ext.param.types.ParamTypeSystem;
import polyglot.ext.param.types.Subst_c;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;

public class JL5Subst_c extends Subst_c implements JL5Subst {

    public JL5Subst_c(ParamTypeSystem ts, Map subst, Map cache) {
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

    // when substituting type variables that aren't in the subst map, 
    // keep track of which ones we have seen already, so that we don't go into 
    // an infinite loop as we subst on their upper bounds.
    private LinkedList<TypeVariable> visitedTypeVars = new LinkedList<TypeVariable>();
    
    public Type substTypeVariable(TypeVariable t) {
        if (subst.containsKey(t)) {
            return (Type)subst.get(t);
        }
        if (visitedTypeVars.contains(t)) {
            return t;
        }
        for (TypeVariable k : (Set<TypeVariable>)subst.keySet()) {
            if (((TypeVariable_c)k).uniqueIdentifier == ((TypeVariable_c)t).uniqueIdentifier) {
                return (Type) subst.get(k);
            }
        }
        visitedTypeVars.addLast(t);
        TypeVariable origT = t;
        t = (TypeVariable) t.upperBound((ReferenceType)substType(t.upperBound()));        

        if (visitedTypeVars.removeLast() != origT) {
            throw new InternalCompilerError("Unexpected type variable was last on the list");
        }
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
            if (!hasTypeVariables(pct)) {
                // no parameters to be instantiated!
                return pct;
            }
            return new JL5SubstClassType_c((JL5TypeSystem) ts, t.position(),
                                           pct, this);
        }

        throw new InternalCompilerError("Don't know how to handle class type " + t.getClass());

    }

    /**
     * Does pct, or a containing class of pct, have type variables?
     */
    private boolean hasTypeVariables(JL5ParsedClassType pct) {
        if (!pct.typeVariables().isEmpty()) {
            return true;
        }
        if (pct.outer() == null) {
            return false;
        }
        if (pct.outer() instanceof JL5ParsedClassType) {
            return hasTypeVariables((JL5ParsedClassType)pct.outer());
        }
        return true;
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
        // subst the type params
        mj.setTypeParams(this.substTypeList(mj.typeParams()));
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
        // subst the type params
        cj.setTypeParams(this.substTypeList(cj.typeParams()));
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
