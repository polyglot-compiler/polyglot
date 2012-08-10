package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.ArrayInit;
import polyglot.ast.Expr;
import polyglot.ast.NewArray_c;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class JL5NewArray_c extends NewArray_c {

    public JL5NewArray_c(Position pos, TypeNode baseType, List<Expr> dims,
            int addDims, ArrayInit init) {
        super(pos, baseType, dims, addDims, init);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5NewArray_c n = (JL5NewArray_c) super.typeCheck(tc);
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        if (!ts.isReifiable(n.type())) {
            throw new SemanticException("The base type of an array must be reifiable.",
                                        this.position());
        }
        return n;
    }
}
