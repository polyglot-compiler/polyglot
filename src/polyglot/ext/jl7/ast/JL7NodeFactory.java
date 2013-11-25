package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.util.Position;

public interface JL7NodeFactory extends JL5NodeFactory {

    TypeNode AmbUnionType(Position pos, List<TypeNode> alternatives);

    MultiCatch MultiCatch(Position pos, Formal formal,
            List<TypeNode> alternatives, Block body);

}
