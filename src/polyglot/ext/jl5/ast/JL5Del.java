package polyglot.ext.jl5.ast;

import polyglot.ast.JL_c;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.util.CodeWriter;
import polyglot.visit.Translator;

public class JL5Del extends JL_c {

    @Override
    public void translate(CodeWriter w, Translator tr) {
        if (tr instanceof JL5Translator) {
            ((JL5Translator) tr).translateNode(this.node(), w);
        }
        else {
            super.translate(w, tr);
        }
    }

}
