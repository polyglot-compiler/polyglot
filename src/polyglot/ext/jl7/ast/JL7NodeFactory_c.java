package polyglot.ext.jl7.ast;

import polyglot.ext.jl5.ast.JL5NodeFactory_c;

public class JL7NodeFactory_c extends JL5NodeFactory_c implements
        JL7NodeFactory {
    public JL7NodeFactory_c() {
        super(new JL7ExtFactory_c(), new JL7DelFactory_c());
    }

    public JL7NodeFactory_c(JL7ExtFactory extFactory) {
        super(extFactory, new JL7DelFactory_c());
    }

    public JL7NodeFactory_c(JL7ExtFactory extFactory, JL7DelFactory delFactory) {
        super(extFactory, delFactory);
    }

    @Override
    public JL7ExtFactory extFactory() {
        return (JL7ExtFactory) super.extFactory();
    }

    @Override
    public JL7DelFactory delFactory() {
        return (JL7DelFactory) super.delFactory();
    }

}
