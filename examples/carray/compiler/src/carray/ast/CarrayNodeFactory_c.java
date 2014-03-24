package carray.ast;

import polyglot.ast.ArrayTypeNode;
import polyglot.ast.ArrayTypeNode_c;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
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
        return ConstArrayTypeNode(pos, base, null, extFactory());
    }

    protected final ArrayTypeNode ConstArrayTypeNode(Position pos,
            TypeNode base, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e =
                    CarrayAbstractExtFactory_c.extConstArrayTypeNode(extFactory);
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext = composeExts(ext, new CarrayConstArrayTypeNodeExt());
        return new ArrayTypeNode_c(pos, base, ext);
    }
}
