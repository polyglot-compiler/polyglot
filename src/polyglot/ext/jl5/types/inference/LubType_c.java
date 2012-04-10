package polyglot.ext.jl5.types.inference;

import java.util.*;

import com.sun.codemodel.internal.ClassType;

import polyglot.ext.jl5.types.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;


public class LubType_c extends ClassType_c implements LubType {

    protected List<ReferenceType> lubElems;
    public LubType_c(TypeSystem ts, Position pos, List<ReferenceType> lubElems) {
        super(ts, pos);  
        // replace any raw classes with the erased version of the raw class.
        List<ReferenceType> l = null;
        for (int i = 0; i < lubElems.size(); i++) {
            ReferenceType t = lubElems.get(i);
            if (t instanceof RawClass) {
                t = ((RawClass)t).erased();
               if (l == null) {
                   l = new ArrayList<ReferenceType>(lubElems);                   
               }
               l.set(i, t);
            }
        }
        if (l == null) {
            l = lubElems;
        }
        this.lubElems = l;
    }

    public List<ReferenceType> lubElements() {
        return lubElems;
    }
    
    protected Type lubCalculated = null;
    public Type calculateLub() {
        if (lubCalculated == null) {
            lubCalculated = lub_force();
        }
        return lubCalculated;
    }
    
//    @Override
//    public List<ReferenceType> bounds() {
//        return calculateLub().bounds();
//    }

    @Override
    public Kind kind() {
        return LUB;
    }

    
    private ReferenceType lub(Type... a) {
        List<ReferenceType> l = new ArrayList<ReferenceType>();
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        for (Type t : a) {
            l.add((ReferenceType) t);
        }
        return ts.lub(this.position, l);
    }

    private Type lub_force() {
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        Set<Type> st = new HashSet<Type>();
        Set<Type> est = null;
        for (Type u : lubElems) {
            List<Type> u_supers = new ArrayList<Type>(ts.allAncestorsOf((ReferenceType) u));
            st.addAll(u_supers);
            Set<Type> est_of_u = new HashSet<Type>();
            for (Type super_of_u : u_supers) {
                if (super_of_u instanceof JL5SubstClassType) {
                    JL5SubstClassType g = (JL5SubstClassType) super_of_u;
                    est_of_u.add(g.base());
                }
                else est_of_u.add(super_of_u);
            }
            if (est == null) {
                est = new HashSet<Type>();
                est.addAll(est_of_u);
            }
            else {
                est.retainAll(est_of_u);
            }
        }
        Set<Type> mec = new HashSet<Type>(est);
        for (Type e1 : est) {
            for (Type e2 : est) {
                if (!ts.equals(e1,e2) && ts.isSubtype(e2, e1)) {
                    mec.remove(e1);
                    break;
                }
            }
        }
        List<ReferenceType> cand = new ArrayList<ReferenceType>();
        for (Type m : mec) {
            List<Type> inv = new ArrayList<Type>();
            for (Type t : st) {
                if (ts.equals(m, t) || 
                    ((t instanceof JL5SubstClassType) && ((JL5SubstClassType)t).base().equals(m) ) ) {
                    inv.add(t);
                }
            }
            cand.add((ReferenceType) lci(inv));
        }
        try {
            if (ts.checkIntersectionBounds(cand, true)) {
                return ts.intersectionType(this.position, cand);
            }
        } catch (SemanticException e) {
        }
        return ts.Object();
    }

    private Type lci(List<Type> inv) {
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        Type first = inv.get(0);
        if (inv.size() == 1 || first instanceof RawClass || first instanceof JL5ParsedClassType) {
            return first;
        }
        JL5SubstClassType res = (JL5SubstClassType) first;
        for (int i = 1; i < inv.size(); i++) {
            Type next = inv.get(i);
            if (next instanceof RawClass || next instanceof JL5ParsedClassType) {
                return next;
            }
            List<Type> lcta_args = new ArrayList<Type>();
            JL5SubstClassType nextp = (JL5SubstClassType) next;
            for (int argi = 0; argi < res.actuals().size(); argi++) {
                Type a1 = (Type)res.actuals().get(argi);
                Type a2 = (Type)nextp.actuals().get(argi);
                lcta_args.add(lcta(a1,a2));
            }
            try {
                res = (JL5SubstClassType)ts.instantiate(position, res.base(), lcta_args);
            }
            catch (SemanticException e) {
                throw new InternalCompilerError("Unexpected SemanticException", e);
            }
        }
        return res;
    }

    private Type lcta(Type a1, Type a2) {
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        if (a1 instanceof WildCardType) {
            WildCardType a1wc = (WildCardType) a1;
            if (a2 instanceof WildCardType) {
                WildCardType a2wc = (WildCardType) a2;
                // a1 and a2 are wild cards
                if (a1wc.hasLowerBound() && a2wc.hasLowerBound()) {
                    return ts.wildCardType(position, null, glb(a1wc.lowerBound(), a2wc.lowerBound()));
                }
                if (a1wc.hasLowerBound()) {
                    // a1 has lower bound, a2 does not
                    if (a1wc.lowerBound().equals(a2wc.upperBound())) {
                        return a1wc.lowerBound();
                    }
                    else {
                        return ts.wildCardType(position);
                    }
                }
                if (a2wc.hasLowerBound()) {
                    // a2 has lower bound, a1 does not
                    if (a2wc.lowerBound().equals(a1wc.upperBound())) {
                        return a2wc.lowerBound();
                    }
                    else {
                        return ts.wildCardType(position);
                    }
                }
                // neither a1 nor a2 has a lower bound
                return ts.wildCardType(position, ts.lub(position, CollectionUtil.list(a1wc.upperBound(), a2wc.upperBound())), null);
            }
            // a1 is a wildcard, a2 is not
            if (a1wc.hasLowerBound()) {
                return ts.wildCardType(position, null, glb(a1wc.lowerBound(), a2));                
            }
            else {
                return ts.wildCardType(position, lub(a1wc.upperBound(), a2), null);                                
            }                              
        }
        // a1 is not a wildcard
        if (a2 instanceof WildCardType) {
            WildCardType a2wc = (WildCardType) a2;
            // a1 is not a wildcard, a2 is a wildcard
            if (a2wc.hasLowerBound()) {
                return ts.wildCardType(position, null, glb(a1, a2wc.lowerBound()));                
            }
            else {
                return ts.wildCardType(position, lub(a1, a2wc.upperBound()), null);                                
            }                              
        }
        
        // neither a1 nor a2 is a wildcard.
        if (ts.equals(a1, a2)) {
            return a1;
        }
        else {
            return ts.wildCardType(position, ts.lub(position, CollectionUtil.list(a1, a2)), null);
        }
    }

    private ReferenceType glb(Type t1, Type t2) {
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        List<ReferenceType> l = CollectionUtil.list(t1, t2);
        try {
            if (!ts.checkIntersectionBounds(l, true)) {
                return ts.Object();
            }
            else {
                return ts.intersectionType(position, l);
            }
        } catch (SemanticException e) {
            return ts.Object();
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("lub(");
        sb.append(JL5TypeSystem_c.listToString(lubElems));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        for (Type elem : lubElements()) {
            if (!ts.isCastValid(elem, toType)) return false;
        }
        return true;
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
        for (Type elem : lubElements()) {
            if (!ts.isImplicitCastValid(elem, toType)) return null;
        }
        
        LinkedList<Type> chain = new LinkedList<Type>();
        chain.add(this);
        chain.add(toType);
        return chain;
    }

    @Override
    public boolean isSubtypeImpl(Type ancestor) {
        for (Type elem : lubElements()) {
            if (!ts.isSubtype(elem, ancestor)) return false;
        }
        return true;
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public EnumInstance enumConstantNamed(String name) {
        return null;
    }

    @Override
    public List<EnumInstance> enumConstants() {
        return Collections.emptyList();
    }

    @Override
    public String translateAsReceiver(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean inStaticContext() {
        return false;
    }

    @Override
    public void setFlags(Flags flags) {
        throw new UnsupportedOperationException();        
    }

    @Override
    public void setContainer(ReferenceType container) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Job job() {
        throw new UnsupportedOperationException();
   }

    @Override
    public polyglot.types.ClassType outer() {
        return null;
    }

    @Override
    public String name() {
        return this.toString();
    }

    @Override
    public Package package_() {
        return null;
    }

    @Override
    public Flags flags() {
        return Flags.PUBLIC.set(Flags.FINAL);
    }

    @Override
    public List constructors() {
        return Collections.emptyList();
    }

    @Override
    public List memberClasses() {
        return Collections.emptyList();
    }

    @Override
    public List methods() {
        return Collections.emptyList();
    }

    @Override
    public List fields() {
        return Collections.emptyList();
    }

    @Override
    public List interfaces() {
        return Collections.emptyList();
    }

    @Override
    public Type superType() {
        return ts.Object();
    }

}
