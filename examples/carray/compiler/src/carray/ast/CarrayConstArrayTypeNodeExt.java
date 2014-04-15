package carray.ast;

import polyglot.ast.ArrayTypeNode;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.TypeBuilder;
import carray.types.CarrayTypeSystem;

/**
 * A <code>ConstArrayTypeNode</code> is a type node for a non-canonical
 * const array type.
 */
public class CarrayConstArrayTypeNodeExt extends CarrayExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        ArrayTypeNode n = (ArrayTypeNode) this.node();
        CarrayTypeSystem ts = (CarrayTypeSystem) tb.typeSystem();
        return n.type(ts.constArrayOf(n.position(), n.base().type()));
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        ArrayTypeNode n = (ArrayTypeNode) this.node();
        CarrayTypeSystem ts = (CarrayTypeSystem) ar.typeSystem();
        NodeFactory nf = ar.nodeFactory();

        if (!n.base().isDisambiguated()) {
            return n;
        }

        Type baseType = n.base().type();

        if (!baseType.isCanonical()) {
            return n;
        }

        return nf.CanonicalTypeNode(n.position(),
                                    ts.constArrayOf(n.position(), baseType));
    }

    @Override
    public String toString() {
        ArrayTypeNode n = (ArrayTypeNode) this.node();
        return n.base().toString() + "const []";
    }
}
