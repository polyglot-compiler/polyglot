package carray.types;

import polyglot.types.ArrayType;
import polyglot.types.ArrayType_c;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * A <code>ConstArrayType</code> represents an array of base java types,
 * whose elements cannot change after initialization.
 */
public class ConstArrayType_c extends ArrayType_c implements ConstArrayType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected boolean isConst;

    /** Used for deserializing types. */
    protected ConstArrayType_c() {
    }

    public ConstArrayType_c(TypeSystem ts, Position pos, Type base,
            boolean isConst) {
        super(ts, pos, base);
        this.isConst = isConst;
    }

    @Override
    public String toString() {
        return base.toString() + (isConst ? " const" : "") + "[]";
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof ConstArrayType) {
            ConstArrayType t = (ConstArrayType) o;
            return t.isConst() == isConst && ts.equals(base, t.base());
        }

        if (o instanceof ArrayType) {
            ArrayType t = (ArrayType) o;
            return !isConst && ts.equals(base, t.base());
        }

        return false;
    }

    @Override
    public boolean isConst() {
        return isConst;
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        if (toType instanceof ConstArrayType
                && ((ConstArrayType) toType).isConst()) {
            // int const[] = int[] 
            return super.isImplicitCastValidImpl(toType);
        }

        // From this point, toType is not a const array

        if (!isConst) {
            if (toType.isArray()) {
                // non-const arrays are invariant.
                return ts.equals(this, toType);
            }
            else {
                // Object = int[] 
                return super.isImplicitCastValidImpl(toType);
            }
        }

        return false;
    }
}
