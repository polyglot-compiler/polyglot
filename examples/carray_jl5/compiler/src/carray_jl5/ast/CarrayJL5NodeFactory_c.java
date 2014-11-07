package carray_jl5.ast;

import polyglot.ast.ArrayTypeNode;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5ExtFactory_c;
import polyglot.ext.jl5.ast.JL5NodeFactory_c;
import polyglot.util.Position;
import carray.ast.CarrayConstArrayTypeNodeExt;
import carray.ast.CarrayExtFactory_c;

/**
 * NodeFactory for carray_jl5 extension.
 *
 */
public class CarrayJL5NodeFactory_c extends JL5NodeFactory_c implements
CarrayJL5NodeFactory {
    public CarrayJL5NodeFactory_c() {
        super(CarrayJL5Lang_c.instance,
              new CarrayJL5ExtFactory_c(new CarrayExtFactory_c(new JL5ExtFactory_c())));
    }

    @Override
    public CarrayJL5ExtFactory extFactory() {
        return (CarrayJL5ExtFactory) super.extFactory();
    }

    @Override
    public ArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base) {
        return ConstArrayTypeNode(pos, base, null, extFactory());
    }

    protected final ArrayTypeNode ConstArrayTypeNode(Position pos,
            TypeNode base, Ext ext, ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e =
                    CarrayJL5AbstractExtFactory_c.extConstArrayTypeNode(extFactory);
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext = composeExts(ext, new CarrayConstArrayTypeNodeExt());
        // FIXME
        return super.ArrayTypeNode(pos, base);
    }
}
