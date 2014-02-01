package polyglot.ext.jl5.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.Translator;

public class JL5Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static JL5Ext ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof JL5Ext)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No JL5 extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (JL5Ext) e;
    }

    @Override
    public final J5Lang lang() {
        return J5Lang_c.instance;
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        if (tr instanceof JL5Translator)
            ((JL5Translator) tr).translateNode(this.node(), w);
        else superLang().translate(this.node(), w, tr);
    }
}
