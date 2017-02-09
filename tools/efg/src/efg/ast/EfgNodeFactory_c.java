package efg.ast;

import polyglot.ext.jl7.ast.JL7NodeFactory_c;

/**
 * NodeFactory for Efg extension.
 */
public class EfgNodeFactory_c extends JL7NodeFactory_c
        implements EfgNodeFactory {
    public EfgNodeFactory_c() {
        this(EfgLang_c.INSTANCE);
    }

    public EfgNodeFactory_c(EfgLang lang) {
        super(lang);
    }

    public EfgNodeFactory_c(EfgLang lang, EfgExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public EfgExtFactory extFactory() {
        return (EfgExtFactory) super.extFactory();
    }

    @Override
    public EfgLang lang() {
        return (EfgLang) super.lang();
    }
}
