package polyglot.ext.jl.qq;

import java_cup.runtime.Symbol;
import polyglot.ast.*;

public interface QQParser {
    public Symbol qq_expr() throws Exception;
    public Symbol qq_stmt() throws Exception;
    public Symbol qq_type() throws Exception;
    public Symbol qq_decl() throws Exception;
    public Symbol qq_file() throws Exception;
    public Symbol qq_member() throws Exception;
}
