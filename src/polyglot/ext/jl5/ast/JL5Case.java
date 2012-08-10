package polyglot.ext.jl5.ast;

import polyglot.ast.Case;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.visit.TypeChecker;

public interface JL5Case extends Case {

    Node resolveCaseLabel(TypeChecker tc, Type switchType)
            throws SemanticException;

}
