package polyglot.ext.jl5.ast;

import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ast.Special_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.RawClass;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class JL5Special_c extends Special_c implements Special {

    public JL5Special_c(Position pos, Kind kind, TypeNode qualifier) {
        super(pos, kind, qualifier);
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        Special n = (Special) visitChildren(tc);
        if (n.qualifier() != null && n.qualifier().type() instanceof RawClass) {
            // we got a raw class. Fix it up
            RawClass rc = (RawClass) n.qualifier().type();
            n = n.qualifier(n.qualifier().type(rc.base()));
        }
        return n.typeCheck(tc);
    }

}
