package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.Term;

public interface ElementValuePair extends Term {
    public String name();

    public Id id();

    public Expr value();
}
