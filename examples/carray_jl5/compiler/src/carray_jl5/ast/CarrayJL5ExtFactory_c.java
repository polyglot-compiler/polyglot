package carray_jl5.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public final class CarrayJL5ExtFactory_c extends CarrayJL5AbstractExtFactory_c {

    public CarrayJL5ExtFactory_c() {
        super();
    }

    public CarrayJL5ExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        CarrayJL5Ext ext = new CarrayJL5Ext();
        return ext;
    }
}
