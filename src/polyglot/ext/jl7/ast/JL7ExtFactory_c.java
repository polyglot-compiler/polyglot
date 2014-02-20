package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public final class JL7ExtFactory_c extends JL7AbstractExtFactory_c {

    public JL7ExtFactory_c() {
        super();
    }

    public JL7ExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        JL7Ext ext = new JL7Ext();
        return ext;
    }

    @Override
    protected Ext extCaseImpl() {
        JL7CaseExt ext = new JL7CaseExt();
        return ext;
    }

    @Override
    protected Ext extNewImpl() {
        JL7NewExt ext = new JL7NewExt();
        return ext;
    }

    @Override
    protected Ext extSwitchImpl() {
        JL7SwitchExt ext = new JL7SwitchExt();
        return ext;
    }

    @Override
    protected Ext extThrowImpl() {
        JL7ThrowExt ext = new JL7ThrowExt();
        return ext;
    }

    @Override
    protected Ext extTryImpl() {
        JL7TryExt ext = new JL7TryExt();
        return ext;
    }
}
