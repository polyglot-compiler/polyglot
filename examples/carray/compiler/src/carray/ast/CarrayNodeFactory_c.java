package carray.ast;

import polyglot.ast.ArrayTypeNode;
import polyglot.ast.NodeFactory_c;
import polyglot.ast.TypeNode;
import polyglot.util.Position;

/**
 * NodeFactory for carray extension.
 *
 */
public class CarrayNodeFactory_c extends NodeFactory_c implements
        CarrayNodeFactory {
    public CarrayNodeFactory_c() {
        super(CarrayLang_c.instance, new CarrayExtFactory_c());
    }

    @Override
    public CarrayExtFactory extFactory() {
        return (CarrayExtFactory) super.extFactory();
    }

    @Override
    public ArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base) {
        ArrayTypeNode n = ArrayTypeNode(pos, base);
        n = (ArrayTypeNode) n.ext(extFactory().extConstArrayTypeNode());
        return n;
    }

}
