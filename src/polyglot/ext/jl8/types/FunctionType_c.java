package polyglot.ext.jl8.types;

import java.util.List;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.Type_c;
import polyglot.util.SerialVersionUID;

public class FunctionType_c extends Type_c implements FunctionType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private List<? extends Type> formalTypes;
    private Type returnType;

    /** Used for deserializing types. */
    protected FunctionType_c() {
        super();
    }

    public FunctionType_c(TypeSystem ts, List<? extends Type> formalTypes, Type returnType) {
        super(ts);
        this.formalTypes = formalTypes;
        this.returnType = returnType;
    }

    @Override
    public List<? extends Type> formalTypes() {
        return formalTypes;
    }

    @Override
    public Type returnType() {
        return returnType;
    }

    @Override
    public String translate(Resolver c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        List<? extends Type> formals = formalTypes();
        for (Type t : formals) sb.append(t).append(", ");
        if (!formals.isEmpty()) sb.setLength(sb.length() - 2);
        sb.append(") -> ").append(returnType());
        return sb.toString();
    }
}
