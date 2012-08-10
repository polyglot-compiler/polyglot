package polyglot.ext.jl5.types;

import java.util.Collections;

import polyglot.types.ArrayType;
import polyglot.types.ArrayType_c;
import polyglot.types.MethodInstance;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class JL5ArrayType_c extends ArrayType_c implements JL5ArrayType {

    protected boolean isVarArg;

    public JL5ArrayType_c(TypeSystem ts, Position pos, Type base,
            boolean isVarargs) {
        super(ts, pos, base);
        this.isVarArg = isVarargs;
    }

    @Override
    protected MethodInstance createCloneMethodInstance() {
        return ts.methodInstance(position(), this, ts.Public(), this, // clone returns this type
                                 "clone",
                                 Collections.<Type> emptyList(),
                                 Collections.<Type> emptyList());
    }

    @Override
    public boolean isVarArg() {
        return this.isVarArg;
    }

    @Override
    public void setVarArg() {
        this.isVarArg = true;
    }

    @Override
    public boolean isSubtypeImpl(Type t) {
        if (super.isSubtypeImpl(t)) {
            return true;
        }

        /* See JLS 3rd Ed 4.10 */
        if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            return this.base().isSubtype(at.base());
        }

        return false;
    }

}
