package jltools.ext.carray.ast;

import jltools.ext.carray.ast.*;
import jltools.ext.carray.types.*;
import jltools.ext.jl.ast.*;
import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Main;
import java.io.IOException;

/**
 * A <code>ConstArrayTypeNode</code> is a type node for a non-canonical
 * const array type.
 */
public class ConstArrayTypeNode_c extends ArrayTypeNode_c implements ConstArrayTypeNode
{
    public ConstArrayTypeNode_c(Ext ext, Position pos, TypeNode base) {
        super(ext, pos, base);
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
        CarrayTypeSystem ts = (CarrayTypeSystem)tb.typeSystem();
        return type(ts.constArrayOf(position(), base.type()));
    }

    public Node disambiguate_(AmbiguityRemover ar) throws SemanticException {
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
