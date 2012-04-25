package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.Id;

public interface ElementValuePair extends Expr {
    public String name();
    public Id id();
    public Expr value();
}
