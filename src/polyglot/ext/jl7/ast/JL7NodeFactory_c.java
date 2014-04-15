package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5NodeFactory_c;
import polyglot.util.Position;

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

    @Override
    public TypeNode AmbUnionType(Position pos, List<TypeNode> alternatives) {
        return new AmbUnionType(pos, alternatives);
    }

    @Override
    public MultiCatch MultiCatch(Position pos, Formal formal,
            List<TypeNode> alternatives, Block body) {
        MultiCatch n = new MultiCatch_c(pos, formal, alternatives, body);
        n = (MultiCatch) n.ext(extFactory().extMultiCatch());
        n = (MultiCatch) n.del(delFactory().delMultiCatch());
        return n;
    }

}
