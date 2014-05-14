package skelpkg.ast;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public abstract class SkelAbstractExtFactory_c extends AbstractExtFactory_c
        implements SkelExtFactory {

    public SkelAbstractExtFactory_c() {
        super();
    }

    public SkelAbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    // TODO: Implement factory methods for new extension nodes in future
    // extensions.  This entails calling the factory method for extension's
    // AST superclass.
}
