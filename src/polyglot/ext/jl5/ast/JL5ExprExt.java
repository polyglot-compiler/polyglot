package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.ExprOps;
import polyglot.util.SerialVersionUID;
import polyglot.visit.Traverser;

public class JL5ExprExt extends JL5TermExt implements ExprOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Expr node() {
        return (Expr) super.node();
    }

    @Override
    public boolean constantValueSet(Traverser v) {
        return v.superLang(lang()).constantValueSet(node(), v);
    }

    @Override
    public boolean isConstant(Traverser v) {
        return v.superLang(lang()).isConstant(node(), v);
    }

    @Override
    public Object constantValue(Traverser v) {
        return v.superLang(lang()).constantValue(node(), v);
    }
}
