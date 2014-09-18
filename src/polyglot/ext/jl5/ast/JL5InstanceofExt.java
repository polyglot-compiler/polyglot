package polyglot.ext.jl5.ast;

import polyglot.ast.Instanceof;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

public class JL5InstanceofExt extends JL5ExprExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Instanceof n = (Instanceof) super.typeCheck(tc);
        TypeNode compareTN = n.compareType();
        if (!compareTN.isTypeChecked()) return n;
        Type compareType = compareTN.type();
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        // JLS 3rd Ed. | 15.20.2
        // It is a compile-time error if compareType does not denote a
        // reifiable type.
        if (!ts.isReifiable(compareType))
            throw new SemanticException("The type for instanceof is not reifiable: "
                    + compareType,
                    compareTN.position());
        return n;
    }
}
