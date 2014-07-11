package polyglot.ext.jl5.ast;

import polyglot.ast.JLang;
import polyglot.ast.Loop;
import polyglot.ast.LoopOps;
import polyglot.ast.Term;
import polyglot.util.SerialVersionUID;
import polyglot.visit.Traverser;

public class JL5LoopExt extends JL5TermExt implements LoopOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Loop node() {
        return (Loop) super.node();
    }

    @Override
    public boolean condIsConstant(Traverser v) {
        return ((JLang) v.superLang(lang())).condIsConstant(node(), v);
    }

    @Override
    public boolean condIsConstantTrue(Traverser v) {
        return ((JLang) v.superLang(lang())).condIsConstantTrue(node(), v);
    }

    @Override
    public boolean condIsConstantFalse(Traverser v) {
        return ((JLang) v.superLang(lang())).condIsConstantFalse(node(), v);
    }

    @Override
    public Term continueTarget(Traverser v) {
        return ((JLang) v.superLang(lang())).continueTarget(node(), v);
    }
}
