package carray.ast;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public abstract class CarrayAbstractExtFactory_c extends AbstractExtFactory_c
implements CarrayExtFactory {

    public CarrayAbstractExtFactory_c() {
        super();
    }

    public CarrayAbstractExtFactory_c(ExtFactory nextExtFactory) {
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
        return extArrayTypeNodeImpl();
    }

    protected Ext postExtConstArrayTypeNode(Ext ext) {
        return postExtArrayTypeNode(ext);
    }
}
