package polyglot.ext.carray.ast;

import polyglot.ext.carray.ast.*;
import polyglot.ext.carray.types.*;
import polyglot.ext.jl.ast.*;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.main.Main;
import java.io.IOException;

/**
 * A <code>ConstArrayTypeNode</code> is a type node for a non-canonical
 * const array type.
 */
public class ConstArrayTypeNode_c extends ArrayTypeNode_c implements ConstArrayTypeNode
{
    public ConstArrayTypeNode_c(Position pos, TypeNode base) {
        super(pos, base);
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        CarrayTypeSystem ts = (CarrayTypeSystem)tb.typeSystem();
        return type(ts.constArrayOf(position(), base.type()));
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        CarrayTypeSystem ts = (CarrayTypeSystem)ar.typeSystem();
        NodeFactory nf = ar.nodeFactory();

        Type baseType = base.type();

        if (! baseType.isCanonical()) {
            throw new SemanticException(
                "Base type " + baseType + " of array could not be resolved.",
                base.position());
        }

        return nf.CanonicalTypeNode(position(),
                                    ts.constArrayOf(position(), baseType));
    }


    public String toString() {
        return base.toString() + "const []";
    }
}
