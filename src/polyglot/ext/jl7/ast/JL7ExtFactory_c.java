package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ext.jl5.ast.JL5ExtFactory_c;

public class JL7ExtFactory_c extends JL5ExtFactory_c implements JL7ExtFactory {
    public JL7ExtFactory_c() {
        super();
    }

    public JL7ExtFactory_c(JL7ExtFactory extFactory) {
        super(extFactory);
    }

    @Override
    public JL7ExtFactory nextExtFactory() {
        return (JL7ExtFactory) super.nextExtFactory();
    }

    @Override
    protected Ext extCaseImpl() {
        return new JL7CaseExt();
    }

    @Override
    protected Ext extSwitchImpl() {
        return new JL7SwitchExt();
    }

}
