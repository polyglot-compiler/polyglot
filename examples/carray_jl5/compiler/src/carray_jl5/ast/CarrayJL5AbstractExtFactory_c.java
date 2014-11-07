package carray_jl5.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ext.jl5.ast.JL5AbstractExtFactory_c;
import carray.ast.CarrayExtFactory;

public abstract class CarrayJL5AbstractExtFactory_c extends
        JL5AbstractExtFactory_c implements CarrayJL5ExtFactory {

    public CarrayJL5AbstractExtFactory_c() {
        super();
    }

    public CarrayJL5AbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    public final Ext extConstArrayTypeNode() {
        Ext e = extConstArrayTypeNodeImpl();
        return postExtConstArrayTypeNode(e);
    }

    protected static final Ext extConstArrayTypeNode(ExtFactory extFactory) {
        if (extFactory instanceof CarrayExtFactory)
            return ((CarrayExtFactory) extFactory).extConstArrayTypeNode();
        return extFactory.extArrayTypeNode();
    }

    protected Ext extConstArrayTypeNodeImpl() {
        return this.extArrayTypeNodeImpl();
    }

    protected Ext postExtConstArrayTypeNode(Ext ext) {
        return postExtArrayTypeNode(ext);
    }
}
