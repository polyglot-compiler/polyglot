package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ast.JLang;

public final class JL7ExtFactory_c extends JL7AbstractExtFactory_c implements
        JL7ExtFactory {
    protected JLang superLang = null;

    public JL7ExtFactory_c() {
        super();
    }

    public JL7ExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    public JL7ExtFactory_c(ExtFactory nextExtFactory, JLang superLang) {
        super(nextExtFactory);
        this.superLang = superLang;
    }

    @Override
    protected Ext extNodeImpl() {
        JL7Ext ext = new JL7Ext();
        ext.superLang(this.superLang);
        return ext;
    }

    @Override
    protected Ext extCaseImpl() {
        JL7CaseExt ext = new JL7CaseExt();
        ext.superLang(this.superLang);
        return ext;
    }

    @Override
    protected Ext extNewImpl() {
        JL7NewExt ext = new JL7NewExt();
        ext.superLang(this.superLang);
        return ext;
    }

    @Override
    protected Ext extSwitchImpl() {
        JL7SwitchExt ext = new JL7SwitchExt();
        ext.superLang(this.superLang);
        return ext;
    }

    @Override
    protected Ext extThrowImpl() {
        JL7ThrowExt ext = new JL7ThrowExt();
        ext.superLang(this.superLang);
        return ext;
    }

    @Override
    protected Ext extTryImpl() {
        JL7TryExt ext = new JL7TryExt();
        ext.superLang(this.superLang);
        return ext;
    }
}
