package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.JLang;
import polyglot.ast.Term;
import polyglot.ast.TermOps;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.Traverser;

public class JL7TermExt extends JL7Ext implements TermOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Term node() {
        return (Term) super.node();
    }

    @Override
    public Term firstChild(Traverser v) {
        return ((JLang) v.superLang(lang())).firstChild(node(), v);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return v.superLang(lang()).acceptCFG(node(), v, succs);
    }
}
