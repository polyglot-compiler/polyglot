package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.List;

import polyglot.types.FieldInstance;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.UnknownType_c;

public class UnknownReferenceType_c extends UnknownType_c implements
        UnknownReferenceType {

    public UnknownReferenceType_c(TypeSystem ts) {
        super(ts);
    }

    @Override
    public Type superType() {
        return this;
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends MemberInstance> members() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends FieldInstance> fields() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends MethodInstance> methods() {
        return Collections.emptyList();
    }

    @Override
    public FieldInstance fieldNamed(String name) {
        return null;
    }

    @Override
    public List<MethodInstance> methodsNamed(String name) {
        return Collections.emptyList();
    }

    @Override
    public List<? extends MethodInstance> methods(String name,
            List<? extends Type> argTypes) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasMethod(MethodInstance mi) {
        return false;
    }

    @Override
    public boolean hasMethodImpl(MethodInstance mi) {
        return false;
    }
}
