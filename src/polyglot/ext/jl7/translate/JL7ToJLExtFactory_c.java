package polyglot.ext.jl7.translate;

import polyglot.ast.Ext;
import polyglot.ext.jl5.translate.JL5ToJLExtFactory_c;
import polyglot.ext.jl7.ast.JL7ExtFactory;

public class JL7ToJLExtFactory_c extends JL5ToJLExtFactory_c implements
        JL7ExtFactory {

    public JL7ToJLExtFactory_c() {
        super();
    }

    public JL7ToJLExtFactory_c(JL7ExtFactory extFactory) {
        super(extFactory);
    }

    @Override
    public Ext extAmbDiamondTypeNode() {
        return extTypeNode();
    }

    @Override
    public Ext extAmbUnionType() {
        return extTypeNode();
    }

    @Override
    public Ext extMultiCatch() {
        return extCatch();
    }

    @Override
    public Ext extResource() {
        return extLocalDecl();
    }

    @Override
    public Ext extTryWithResources() {
        return extTry();
    }
}
