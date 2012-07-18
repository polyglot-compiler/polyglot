package polyglot.ext.jl5.types;

import java.util.*;

import polyglot.ext.param.types.ParamTypeSystem;
import polyglot.ext.param.types.Subst_c;
import polyglot.ext.param.types.Subst_c.TypeXform;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.CachingTransformingList;
import polyglot.util.InternalCompilerError;
import polyglot.util.Transformation;

/**
 * Subst for a raw type (See JLS 3rd ed Sec 4.8.)
 * Some substitution behavior differs for raw types.
 *
 */
public class JL5RawSubst_c extends JL5Subst_c implements JL5Subst {
    private final JL5ParsedClassType base;
    public JL5RawSubst_c(JL5TypeSystem ts, Map subst, JL5ParsedClassType base) {
        super(ts, subst);
        this.base = base;
    }
    
    @Override
    public ClassType substClassType(ClassType t) {
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
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
    public MethodInstance substMethod(MethodInstance mi) {
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        if (!base.equals(mi.container())) {
            return super.substMethod(mi);
        }
        
        // mi is a member of the raw class we are substituting.
        if (mi.flags().isStatic()) {
            // static method!
            // JLS 3rd ed 4.8: "The type of a static member of a raw type C is the same as its type in the generic declaration corresponding to C."
            return (MethodInstance)mi.declaration();
        }
        // The type of a constructor (�8.8), instance method (�8.8, �9.4), or non-static field (�8.3) M 
        // of a raw type C that is not inherited from its superclasses or super- interfaces is the erasure of 
        // its type in the generic declaration corresponding to C.
        
        JL5MethodInstance mj = (JL5MethodInstance)mi.declaration();
        
        Type rt = ts.erasureType(mj.returnType());

        List formalTypes = mj.formalTypes();
        formalTypes = eraseTypeList(formalTypes);

        List throwTypes = mj.throwTypes();
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
            tmpMi = (JL5MethodInstance)eraseMI.substMethod(tmpMi);
        }

        return tmpMi;
    }

    @Override
    public ConstructorInstance substConstructor(ConstructorInstance ci) {
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        if (!base.equals(ci.container())) {
            return super.substConstructor(ci);
        }
        
        // ci is a member of the raw class we are substituting.

        // The type of a constructor (�8.8), instance method (�8.8, �9.4), or non-static field (�8.3) M 
        // of a raw type C that is not inherited from its superclasses or super- interfaces is the erasure of 
        // its type in the generic declaration corresponding to C.
        
        JL5ConstructorInstance cj = (JL5ConstructorInstance)ci.declaration();
        
        List formalTypes = cj.formalTypes();
        formalTypes = eraseTypeList(formalTypes);

        List throwTypes = cj.throwTypes();
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
            tmpCi = (JL5ConstructorInstance)eraseCI.substConstructor(tmpCi);
        }
        return tmpCi;
    }

    public List<Type> eraseTypeList(List<Type> list) {
        return new CachingTransformingList(list, new TypeErase());
    }
    
    /** Function object for transforming types. */
    private class TypeErase implements Transformation {
        public Object transform(Object o) {
            if (! (o instanceof Type)) {
                throw new InternalCompilerError(o + " is not a type.");
            }

            return ((JL5TypeSystem)ts).erasureType((Type) o);
        }
    }


}
