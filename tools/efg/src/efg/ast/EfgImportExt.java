package efg.ast;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.JL5Import;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

public class EfgImportExt extends EfgExt {

    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Import n = (Import) node();
        if (n.kind() == JL5Import.SINGLE_STATIC_MEMBER) {
            return n;
        }

        return superLang().typeCheck(n, tc);
    }

}
