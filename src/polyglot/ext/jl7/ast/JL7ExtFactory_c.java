package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ast.JLDel;

public final class JL7ExtFactory_c extends JL7AbstractExtFactory_c implements
        JL7ExtFactory {
    protected JLDel superDel = null;

    public JL7ExtFactory_c() {
        super();
    }

    public JL7ExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    public JL7ExtFactory_c(ExtFactory nextExtFactory, JLDel superDel) {
        super(nextExtFactory);
        this.superDel = superDel;
    }

    @Override
    protected Ext extNodeImpl() {
        JL7Ext ext = new JL7Ext();
        ext.superDel(this.superDel);
        return ext;
    }

    @Override
    protected Ext extCaseImpl() {
        JL7CaseExt ext = new JL7CaseExt();
        ext.superDel(this.superDel);
        return ext;
    }

    @Override
    protected Ext extNewImpl() {
        JL7NewExt ext = new JL7NewExt();
        ext.superDel(this.superDel);
        return ext;
    }

    @Override
    protected Ext extSwitchImpl() {
        JL7SwitchExt ext = new JL7SwitchExt();
        ext.superDel(this.superDel);
        return ext;
    }

    @Override
    protected Ext extThrowImpl() {
        JL7ThrowExt ext = new JL7ThrowExt();
        ext.superDel(this.superDel);
        return ext;
    }

    @Override
    protected Ext extTryImpl() {
        JL7TryExt ext = new JL7TryExt();
        ext.superDel(this.superDel);
        return ext;
    }
}
