package polyglot.ext.jl5.types;

import java.util.List;
import java.util.Map;

import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.CachingTransformingList;
import polyglot.util.Transformation;

/**
 * Subst for a raw type (See JLS 3rd ed Sec 4.8.)
 * Some substitution behavior differs for raw types.
 *
 */
public class JL5RawSubst_c extends JL5Subst_c implements JL5Subst {
    private final JL5ParsedClassType base;

    public JL5RawSubst_c(JL5TypeSystem ts,
            Map<TypeVariable, ReferenceType> subst, JL5ParsedClassType base) {
        super(ts, subst);
        this.base = base;
    }

    @Override
    public ClassType substClassType(ClassType t) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        if (base.equals(t)) {
            return ts.rawClass(base);
        }

        return super.substClassType(t);
//        // Don't bother trying to substitute into a non-JL5 class.
//        if (! (t instanceof JL5ClassType)) {
//            return t;
//        }
//
//        if (t instanceof RawClass) {
//            // don't substitute raw classes
//            return t;
//        }
//        if (t instanceof JL5SubstClassType) {
//            // this case should be impossible
//            throw new InternalCompilerError("Should have no JL5SubstClassTypes");
//        }
//        
//        if (t instanceof JL5ParsedClassType) {
//            JL5ParsedClassType pct = (JL5ParsedClassType)t;            
//            if (pct.typeVariables().isEmpty()) {
//                // no parameters to be instantiated!
//                return pct;
//            }
//            return new JL5SubstClassType_c((JL5TypeSystem) ts, t.position(),
//                                           (JL5ParsedClassType) t, this);
//        }
//
//        throw new InternalCompilerError("Don't know how to handle class type " + t.getClass());

    }

    @Override
    public <T extends MethodInstance> T substMethod(T mi) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        if (!base.equals(mi.container())) {
            return super.substMethod(mi);
        }

        // mi is a member of the raw class we are substituting.
        if (mi.flags().isStatic()) {
            // static method!
            // JLS 3rd ed 4.8: "The type of a static member of a raw type C is the same as its type in the generic declaration corresponding to C."
            @SuppressWarnings("unchecked")
            T result = (T) mi.declaration();
            return result;
        }
        // The type of a constructor (�8.8), instance method (�8.8, �9.4), or non-static field (�8.3) M 
        // of a raw type C that is not inherited from its superclasses or super- interfaces is the erasure of 
        // its type in the generic declaration corresponding to C.

        JL5MethodInstance mj = (JL5MethodInstance) mi.declaration();

        Type rt = ts.erasureType(mj.returnType());

        List<? extends Type> formalTypes = mj.formalTypes();
        formalTypes = eraseTypeList(formalTypes);

        List<? extends Type> throwTypes = mj.throwTypes();
        throwTypes = eraseTypeList(throwTypes);

        JL5MethodInstance tmpMi = (JL5MethodInstance) mj.copy();
        tmpMi.setReturnType(rt);
        tmpMi.setFormalTypes(formalTypes);
        tmpMi.setThrowTypes(throwTypes);
        tmpMi.setContainer(ts.rawClass(base));

        // subst the type params
        tmpMi.setTypeParams(this.<TypeVariable> substTypeList(tmpMi.typeParams()));

        // now erase the type params, if there are any
        JL5Subst eraseMI = ts.erasureSubst(tmpMi);
        if (eraseMI != null) {
            tmpMi = eraseMI.substMethod(tmpMi);
        }

        @SuppressWarnings("unchecked")
        T result = (T) tmpMi;
        return result;
    }

    @Override
    public <T extends ConstructorInstance> T substConstructor(T ci) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        if (!base.equals(ci.container())) {
            return super.substConstructor(ci);
        }

        // ci is a member of the raw class we are substituting.

        // The type of a constructor (�8.8), instance method (�8.8, �9.4), or non-static field (�8.3) M 
        // of a raw type C that is not inherited from its superclasses or super- interfaces is the erasure of 
        // its type in the generic declaration corresponding to C.

        JL5ConstructorInstance cj = (JL5ConstructorInstance) ci.declaration();

        List<? extends Type> formalTypes = cj.formalTypes();
        formalTypes = eraseTypeList(formalTypes);

        List<? extends Type> throwTypes = cj.throwTypes();
        throwTypes = eraseTypeList(throwTypes);

        JL5ConstructorInstance tmpCi = (JL5ConstructorInstance) cj.copy();
        tmpCi.setFormalTypes(formalTypes);
        tmpCi.setThrowTypes(throwTypes);
        tmpCi.setContainer(ts.rawClass(base));

        // subst the type params
        tmpCi.setTypeParams(this.<TypeVariable> substTypeList(tmpCi.typeParams()));

        // now erase the type params, if there are any
        JL5Subst eraseCI = ts.erasureSubst(tmpCi);
        if (eraseCI != null) {
            tmpCi = eraseCI.substConstructor(tmpCi);
        }

        @SuppressWarnings("unchecked")
        T result = (T) tmpCi;
        return result;
    }

    public List<Type> eraseTypeList(List<? extends Type> list) {
        return new CachingTransformingList<Type, Type>(list, new TypeErase());
    }

    /** Function object for transforming types. */
    private class TypeErase implements Transformation<Type, Type> {
        @Override
        public Type transform(Type o) {
            return ((JL5TypeSystem) ts).erasureType(o);
        }
    }

}
