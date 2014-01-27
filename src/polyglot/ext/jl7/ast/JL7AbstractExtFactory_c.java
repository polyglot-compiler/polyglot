package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ext.jl5.ast.JL5AbstractExtFactory_c;

public abstract class JL7AbstractExtFactory_c extends JL5AbstractExtFactory_c
        implements JL7ExtFactory {

    public JL7AbstractExtFactory_c() {
        super();
    }

    public JL7AbstractExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    public final Ext extAmbDiamondTypeNode() {
        Ext e = extAmbDiamondTypeNodeImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL7ExtFactory) {
                e2 = ((JL7ExtFactory) nextExtFactory()).extAmbDiamondTypeNode();
            }
            else {
                e2 = nextExtFactory().extTypeNode();
            }
            e = composeExts(e, e2);
        }
        return postExtAmbDiamondTypeNode(e);
    }

    @Override
    public final Ext extAmbUnionType() {
        Ext e = extAmbUnionTypeImpl();

        if (nextExtFactory() != null) {
            Ext e2;
            if (nextExtFactory() instanceof JL7ExtFactory) {
                e2 = ((JL7ExtFactory) nextExtFactory()).extAmbUnionType();
            }
            else {
                e2 = nextExtFactory().extTypeNode();
            }
            e = composeExts(e, e2);
        }
        return postExtAmbUnionType(e);
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

    protected Ext extAmbDiamondTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    protected Ext extAmbUnionTypeImpl() {
        return extTypeNodeImpl();
    }

    protected Ext extMultiCatchImpl() {
        return extCatchImpl();
    }

    protected Ext postExtAmbDiamondTypeNode(Ext e) {
        return postExtTypeNode(e);
    }

    protected Ext postExtAmbUnionType(Ext e) {
        return postExtTypeNode(e);
    }

    protected Ext postExtMultiCatch(Ext e) {
        return postExtCatch(e);
    }

}
