package polyglot.ext.jl8.ast;

import java.util.List;
import polyglot.ast.Block;
import polyglot.ast.CodeNode;
import polyglot.ast.Expr;
import polyglot.ast.Formal;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;

public interface Lambda extends Expr, CodeNode {
    List<Formal> formals();

    Lambda formals(List<Formal> formals);

    Block block();

    Lambda block(Block block);

    void setTargetType(Type targetType, JL8TypeSystem typeSystem) throws SemanticException;
}
