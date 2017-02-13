package efg.ast;

import polyglot.ast.ExtFactory;
import polyglot.ext.jl7.ast.JL7AbstractExtFactory_c;

public abstract class EfgAbstractExtFactory_c extends JL7AbstractExtFactory_c
        implements EfgExtFactory {

    public EfgAbstractExtFactory_c() {
        super();
    }

    public EfgAbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }
}
