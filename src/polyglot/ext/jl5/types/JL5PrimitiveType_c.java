package polyglot.ext.jl5.types;

import polyglot.types.PrimitiveType_c;
import polyglot.types.Type;
import polyglot.types.TypeSystem;

@SuppressWarnings("serial")
public class JL5PrimitiveType_c extends PrimitiveType_c implements
        JL5PrimitiveType {

    public JL5PrimitiveType_c(TypeSystem ts, Kind kind) {
        super(ts, kind);
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        if (super.isImplicitCastValidImpl(toType)) {
            return true;
        }

        if (!toType.isPrimitive()) {
            // We can box this primitive in its wrapper type, so check that.
            JL5TypeSystem ts = (JL5TypeSystem) typeSystem();
            Type wrapperType = ts.wrapperClassOfPrimitive(this);
            return ts.isImplicitCastValid(wrapperType, toType);
        }
        return false;
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        if (super.isCastValidImpl(toType)) {
            return true;
        }
        // We can box this primitive in its wrapper type, so check that.
        JL5TypeSystem ts = (JL5TypeSystem) typeSystem();
        Type wrapperType = ts.wrapperClassOfPrimitive(this);
        return ts.isCastValid(wrapperType, toType);
    }
}
