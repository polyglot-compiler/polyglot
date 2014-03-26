package polyglot.ext.jl5.ast;

import polyglot.ast.JLang;
import polyglot.ast.Loop;
import polyglot.ast.LoopOps;
import polyglot.ast.Term;
import polyglot.util.SerialVersionUID;

public class JL5LoopExt extends JL5TermExt implements LoopOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Loop node() {
        return (Loop) super.node();
    }

    @Override
    public boolean condIsConstant(JLang lang) {
        return superLang().condIsConstant(node(), lang);
    }

    @Override
    public boolean condIsConstantTrue(JLang lang) {
        return superLang().condIsConstantTrue(node(), lang);
    }

    @Override
    public boolean condIsConstantFalse(JLang lang) {
        return superLang().condIsConstantFalse(node(), lang);
    }

    @Override
    public Term continueTarget() {
        return superLang().continueTarget(node());
    }
}
