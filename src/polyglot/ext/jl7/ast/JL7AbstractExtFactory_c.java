package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ast.JLDel;
import polyglot.ext.jl5.ast.JL5AbstractExtFactory_c;

public abstract class JL7AbstractExtFactory_c extends JL5AbstractExtFactory_c
        implements JL7ExtFactory {
    protected JLDel superDel = null;

    public JL7AbstractExtFactory_c() {
        super();
    }

    public JL7AbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    public JL7AbstractExtFactory_c(ExtFactory nextExtFactory, JLDel superDel) {
        super(nextExtFactory);
        this.superDel = superDel;
    }

    @Override
    public final Ext extMultiCatch() {
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
