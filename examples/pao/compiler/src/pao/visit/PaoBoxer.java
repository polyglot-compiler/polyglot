package polyglot.ext.pao.visit;

import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.extension.*;
import polyglot.ext.pao.types.*;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import java.util.*;

/**
 * Visitor that inserts boxing and unboxing code into the AST.
 */
public class PaoBoxer extends AscriptionVisitor
{
    public PaoBoxer(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    public Expr ascribe(Expr e, Type toType) {
        Type fromType = e.type();

        if (toType == null) {
            return e;
        }

        Position p = e.position();

        // Insert a cast.  Translation of the cast will insert the
        // correct boxing/unboxing code.
        if (toType.isReference() && fromType.isPrimitive()) {
            return nf.Cast(p, nf.CanonicalTypeNode(p, ts.Object()), e);
        }

        return e;
    }

    public Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        n = super.leaveCall(old, n, v);

        if (n.ext() instanceof PaoExt) {
            return ((PaoExt) n.ext()).rewrite((PaoTypeSystem) typeSystem(),
                                              nodeFactory());
        }

        return n;
    }
}
