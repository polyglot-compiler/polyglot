package polyglot.ext.jl5.ast;

import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.visit.TypeChecker;

public interface JL5CaseOps {
    Node resolveCaseLabel(TypeChecker tc, Type switchType)
            throws SemanticException;
}
