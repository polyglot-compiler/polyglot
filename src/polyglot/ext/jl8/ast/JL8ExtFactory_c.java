package polyglot.ext.jl8.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public final class JL8ExtFactory_c extends JL8AbstractExtFactory_c implements JL8ExtFactory {

    public JL8ExtFactory_c() {
        super();
    }

    public JL8ExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        return new JL8Ext();
    }

    // TODO: Override factory methods for new extension nodes in the current
    // extension.
}
