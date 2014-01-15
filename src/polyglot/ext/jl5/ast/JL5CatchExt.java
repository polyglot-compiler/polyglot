package polyglot.ext.jl5.ast;

import polyglot.ast.Catch;
import polyglot.ast.Ext;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5CatchExt extends JL5Ext implements Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {

        Catch c = (Catch) this.node();
        JL5FormalExt formalExt = (JL5FormalExt) JL5Ext.ext(c.formal());
        formalExt.setIsCatchFormal(true);

        return superLang().buildTypes(this.node(), tb);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Catch c = (Catch) this.node();
        Formal f = c.formal();
        JL5LocalInstance li = (JL5LocalInstance) f.localInstance();
        li.setProcedureFormal(true);

        return superLang().typeCheck(this.node(), tc);
    }

}
