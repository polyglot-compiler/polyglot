package polyglot.ext.jl5.ast;

import polyglot.ast.JLDel;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5CatchDel extends JL5Del implements JLDel {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5CatchExt ext = (JL5CatchExt) JL5Ext.ext(this.node());
        return ext.buildTypes(tb);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5CatchExt ext = (JL5CatchExt) JL5Ext.ext(this.node());
        return ext.typeCheck(tc);
    }

}
