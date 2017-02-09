package efg.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public final class EfgExtFactory_c extends EfgAbstractExtFactory_c {

    public EfgExtFactory_c() {
        super();
    }

    public EfgExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        return new EfgExt();
    }

    @Override
    protected Ext extClassDeclImpl() {
        return new EfgClassDeclExt();
    }

    @Override
    protected Ext extImportImpl() {
        return new EfgImportExt();
    }
}
