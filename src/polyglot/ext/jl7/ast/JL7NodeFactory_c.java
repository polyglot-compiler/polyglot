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
        super(new JL7ExtFactory_c(), J7Lang_c.instance);
    }

    public JL7NodeFactory_c(JL7ExtFactory extFactory) {
        super(extFactory, J7Lang_c.instance);
    }

    public JL7NodeFactory_c(JL7ExtFactory extFactory, J7Lang del) {
        super(extFactory, del);
    }

    @Override
    public JL7ExtFactory extFactory() {
        return (JL7ExtFactory) super.extFactory();
    }

    @Override
    public J7Lang lang() {
        return (J7Lang) super.lang();
    }

    @Override
    public AmbDiamondTypeNode AmbDiamondTypeNode(Position pos, TypeNode base) {
        AmbDiamondTypeNode n = new AmbDiamondTypeNode(pos, base);
        n = (AmbDiamondTypeNode) n.ext(extFactory().extAmbDiamondTypeNode());
        return n;
    }

    @Override
    public TypeNode AmbUnionType(Position pos, List<TypeNode> alternatives) {
        AmbUnionType n = new AmbUnionType(pos, alternatives);
        n = (AmbUnionType) n.ext(extFactory().extAmbUnionType());
        return n;
    }

    @Override
    public MultiCatch MultiCatch(Position pos, Formal formal,
            List<TypeNode> alternatives, Block body) {
        MultiCatch n = new MultiCatch_c(pos, formal, alternatives, body);
        n = (MultiCatch) n.ext(extFactory().extMultiCatch());
        return n;
    }

}
