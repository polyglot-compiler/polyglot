package polyglot.ext.jl8.ast;

import java.util.List;
import polyglot.ast.Block;
import polyglot.ast.Expr;
import polyglot.ast.Formal;

public interface Lambda extends Expr {
    List<Formal> formals();

    Lambda formals(List<Formal> formals);

    Block block();

    Lambda block(Block block);
}
