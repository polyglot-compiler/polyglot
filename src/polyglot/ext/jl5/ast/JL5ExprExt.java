package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.ExprOps;
import polyglot.ast.Lang;
import polyglot.util.SerialVersionUID;

public class JL5ExprExt extends JL5TermExt implements ExprOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Expr node() {
        return (Expr) super.node();
    }

    @Override
    public boolean constantValueSet(Lang lang) {
        return superLang().constantValueSet(node(), lang);
    }

    @Override
    public boolean isConstant(Lang lang) {
        return superLang().isConstant(node(), lang);
    }

    @Override
    public Object constantValue(Lang lang) {
        return superLang().constantValue(node(), lang);
    }
}
