package polyglot.ext.jl7.ast;

import polyglot.ast.DelFactory;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ext.jl5.ast.JL5ExtFactory_c;

public class JL7ExtFactory_c extends JL5ExtFactory_c implements JL7ExtFactory {
    protected DelFactory superDelFactory = null;

    public JL7ExtFactory_c() {
        super();
    }

    public JL7ExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    public JL7ExtFactory_c(ExtFactory nextExtFactory, DelFactory superDelFactory) {
        super(nextExtFactory);
        this.superDelFactory = superDelFactory;
    }

    @Override
    protected Ext extCaseImpl() {
        JL7CaseExt ext = new JL7CaseExt();
        ext.setSuperDel(this.superDelFactory.delCase());
        return ext;
    }

    @Override
    protected Ext extSwitchImpl() {
        JL7SwitchExt ext = new JL7SwitchExt();
        ext.setSuperDel(this.superDelFactory.delSwitch());
        return ext;
    }

    @Override
    protected Ext extThrowImpl() {
        JL7ThrowExt ext = new JL7ThrowExt();
        ext.setSuperDel(this.superDelFactory.delThrow());
        return ext;
    }

    @Override
    protected Ext extTryImpl() {
        return new JL7TryExt();
    }

    @Override
    public Ext extMultiCatch() {
        Ext e = extMultiCatchImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL7ExtFactory) {
                e2 = ((JL7ExtFactory) nextExtFactory()).extMultiCatch();
            }
            else {
                e2 = nextExtFactory().extCatch();
            }
            e = composeExts(e, e2);
        }
        return postExtMultiCatch(e);
    }

    protected Ext extMultiCatchImpl() {
        return extCatchImpl();
    }

    protected Ext postExtMultiCatch(Ext e) {
        return postExtCatch(e);
    }

}
