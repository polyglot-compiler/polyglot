package polyglot.ext.jl8.ast;

import polyglot.ast.ExtFactory;
import polyglot.ext.jl7.ast.JL7AbstractExtFactory_c;

public abstract class JL8AbstractExtFactory_c extends JL7AbstractExtFactory_c
        implements JL8ExtFactory {

    public JL8AbstractExtFactory_c() {
        super();
    }

    public JL8AbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    // TODO: Implement factory methods for new extension nodes in future
    // extensions.  This entails calling the factory method for extension's
    // AST superclass.
}
