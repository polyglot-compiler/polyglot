package polyglot.ext.jl5.types;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.ClassType_c;
import polyglot.types.PrimitiveType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;

public abstract class JL5ClassType_c extends ClassType_c implements JL5ClassType {

    public abstract List enumConstants();
    
    public EnumInstance enumConstantNamed(String name){
        for(Iterator it = enumConstants().iterator(); it.hasNext();){
            EnumInstance ei = (EnumInstance)it.next();
            if (ei.name().equals(name)){
                return ei;
            }
        }
        return null;
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType){
        throw new InternalCompilerError("Should not be called in JL5");
    }
    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType){
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        LinkedList<Type> chain = null;
        if (super.isImplicitCastValidImpl(toType)) {
            chain = new LinkedList<Type>();
            chain.add(this);
            chain.add(toType);
        }
        else if (toType.isPrimitive()) {
            // see if unboxing will let us cast to the primitive
            PrimitiveType pt = toType.toPrimitive();
            ClassType wrapperType = ts.wrapperClassOfPrimitive(pt);
            chain = ts.isImplicitCastValidChain(this, wrapperType);
            if (chain != null) {
                chain.addLast(toType);
            }
        }
        return chain;
    }
}
