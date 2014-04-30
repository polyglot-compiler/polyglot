package polyglot.ext.jl5.ast;

import polyglot.ast.Case;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.visit.TypeChecker;

public interface JL5CaseOps {
    Case resolveCaseLabel(TypeChecker tc, Type switchType)
            throws SemanticException;
}
