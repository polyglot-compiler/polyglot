package polyglot.ext.jl5.ast;

import polyglot.ast.Field_c;
import polyglot.ast.Id;
import polyglot.ast.Receiver;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.util.Position;

public class EnumConstant_c extends Field_c implements EnumConstant {

    //protected EnumInstance enumInstance;

    public EnumConstant_c(Position pos, Receiver target, Id name) {
        super(pos, target, name);
    }

    @Override
    public boolean constantValueSet() {
        return true;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public Object constantValue() {
        return enumInstance().ordinal();
    }

    @Override
    public EnumInstance enumInstance() {
        return (EnumInstance) fieldInstance();
    }

    @Override
    public EnumConstant enumInstance(EnumInstance enumInstance) {
        return (EnumConstant) fieldInstance(enumInstance);
    }

}
