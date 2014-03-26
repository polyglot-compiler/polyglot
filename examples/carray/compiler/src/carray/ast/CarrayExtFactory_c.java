package carray.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public final class CarrayExtFactory_c extends CarrayAbstractExtFactory_c {

    public CarrayExtFactory_c() {
        super();
    }

    public CarrayExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        CarrayExt ext = new CarrayExt();
        return ext;
    }

    @Override
    protected Ext extArrayAccessAssignImpl() {
        CarrayAssignExt ext = new CarrayAssignExt();
        return ext;
    }

    @Override
    protected Ext extConstArrayTypeNodeImpl() {
        return null;
    }
}
