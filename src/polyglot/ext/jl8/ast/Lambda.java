package polyglot.ext.jl8.ast;

import polyglot.ast.Expr;

public interface Lambda extends Expr {
    LambdaFunctionDeclaration declaration();

    Lambda declaration(LambdaFunctionDeclaration declaration);
}
