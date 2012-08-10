package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.LocalDecl;
import polyglot.ast.Loop;
import polyglot.ast.Stmt;

public interface ExtendedFor extends Loop {

    ExtendedFor body(Stmt body);

    ExtendedFor decl(LocalDecl decl);

    LocalDecl decl();

    Expr expr();

    ExtendedFor expr(Expr expr);

}
