package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Term;
import polyglot.ast.TermOps;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;

public class JL5TermExt extends JL5Ext implements TermOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Term node() {
        return (Term) super.node();
    }

    @Override
    public Term firstChild() {
        return superLang().firstChild(node());
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return superLang().acceptCFG(node(), v, succs);
    }

}
