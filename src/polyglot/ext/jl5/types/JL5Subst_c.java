package polyglot.ext.jl5.types;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polyglot.ext.param.types.ParamTypeSystem;
import polyglot.ext.param.types.Subst_c;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;

public class JL5Subst_c extends Subst_c<TypeVariable, ReferenceType> implements
        JL5Subst {

    public JL5Subst_c(ParamTypeSystem<TypeVariable, ReferenceType> ts,
            Map<TypeVariable, ? extends ReferenceType> subst) {
        super(ts, subst);
//        if (subst.isEmpty()) {
//            Thread.dumpStack();
//        }
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
            return substIntersectionType((IntersectionType) t);
        }

        return super.substType(t);
    }

    // when substituting type variables that aren't in the subst map, 
    // keep track of which ones we have seen already, so that we don't go into 
    // an infinite loop as we subst on their upper bounds.
    private LinkedList<TypeVariable> visitedTypeVars =
            new LinkedList<TypeVariable>();

    public ReferenceType substTypeVariable(TypeVariable t) {
        if (subst.containsKey(t)) {
            return subst.get(t);
        }
        if (visitedTypeVars.contains(t)) {
            return t;
        }
        for (TypeVariable k : subst.keySet()) {
            if (((TypeVariable_c) k).uniqueIdentifier == ((TypeVariable_c) t).uniqueIdentifier) {
                return subst.get(k);
            }
        }
        visitedTypeVars.addLast(t);
        TypeVariable origT = t;
        t = t.upperBound((ReferenceType) substType(t.upperBound()));

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
        t = (IntersectionType) t.copy();
        t.setBounds(s);
        return t;
    }

    @Override
    protected ReferenceType substSubstValue(ReferenceType value) {
        return (ReferenceType) substType(value);
    }

    @Override
    public ClassType substClassType(ClassType t) {
        // Don't bother trying to substitute into a non-JL5 class.
        if (!(t instanceof JL5ClassType)) {
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
            JL5ParsedClassType pct = (JL5ParsedClassType) t;
            JL5TypeSystem ts = (JL5TypeSystem) this.ts;
            List<TypeVariable> typeVars =
                    ts.classAndEnclosingTypeVariables(pct);
            // are the type variables of pct actually relevant to this subst? If not, then return the pct.
            boolean typeVarsRelevant = false;
            for (TypeVariable tv : typeVars) {
                if (this.substitutions().containsKey(tv)) {
                    typeVarsRelevant = true;
                    break;
                }
            }
            if (!typeVarsRelevant) {
                // no parameters to be instantiated!
                return pct;
            }

            return new JL5SubstClassType_c(ts, t.position(), pct, this);
        }

        throw new InternalCompilerError("Don't know how to handle class type "
                + t.getClass());

    }

    @Override
    public <T extends MethodInstance> T substMethod(T mi) {
        JL5MethodInstance mj = (JL5MethodInstance) super.substMethod(mi);
        if (mj.typeParams() != null && !mj.typeParams().isEmpty()) {
            // remove any type params we have replced.
            List<TypeVariable> l = new ArrayList<TypeVariable>(mj.typeParams());
            l.removeAll(this.subst.keySet());
            mj = (JL5MethodInstance) mj.copy();
            mj.setTypeParams(l);
        }
        // subst the type params
        mj.setTypeParams(this.<TypeVariable> substTypeList(mj.typeParams()));

        @SuppressWarnings("unchecked")
        T result = (T) mj;
        return result;
    }

    @Override
    public <T extends ConstructorInstance> T substConstructor(T ci) {
        JL5ConstructorInstance cj =
                (JL5ConstructorInstance) super.substConstructor(ci);
        if (cj.typeParams() != null && !cj.typeParams().isEmpty()) {
            // remove any type params we have replced.
            List<TypeVariable> l = new ArrayList<TypeVariable>(cj.typeParams());
            l.removeAll(this.subst.keySet());
            cj = (JL5ConstructorInstance) cj.copy();
            cj.setTypeParams(l);
        }

        // subst the type params
        cj.setTypeParams(this.<TypeVariable> substTypeList(cj.typeParams()));

        @SuppressWarnings("unchecked")
        T result = (T) cj;
        return result;
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
