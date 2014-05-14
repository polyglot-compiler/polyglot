package skelpkg.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public final class SkelExtFactory_c extends SkelAbstractExtFactory_c {

    public SkelExtFactory_c() {
        super();
    }

    public SkelExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        return new SkelExt();
    }

    // TODO: Override factory methods for new extension nodes in the current
    // extension.
}
