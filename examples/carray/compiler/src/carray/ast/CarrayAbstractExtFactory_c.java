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

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof CarrayExtFactory) {
                e2 =
                        ((CarrayExtFactory) nextExtFactory()).extConstArrayTypeNode();
            }
            else {
                e2 = nextExtFactory().extArrayTypeNode();
            }
            e = composeExts(e, e2);
        }
        return postExtConstArrayTypeNode(e);
    }

    protected Ext extConstArrayTypeNodeImpl() {
        return this.extArrayTypeNodeImpl();
    }

    protected Ext postExtConstArrayTypeNode(Ext ext) {
        return postExtArrayTypeNode(ext);
    }
}
