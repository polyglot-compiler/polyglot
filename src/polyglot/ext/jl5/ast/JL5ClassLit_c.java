package polyglot.ext.jl5.ast;

import polyglot.ast.ClassLit_c;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class JL5ClassLit_c extends ClassLit_c {

    public JL5ClassLit_c(Position pos, TypeNode typeNode) {
        super(pos, typeNode);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        return type(ts.Class(this.position(), (ReferenceType) typeNode().type()));
    }

}
