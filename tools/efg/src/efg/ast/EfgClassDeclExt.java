package efg.ast;

import efg.types.EfgTypeSystem;
import efg.visit.EFGInfoCollector;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassDeclOps;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.ConstructorInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

public class EfgClassDeclExt extends EfgExt implements ClassDeclOps {

    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public ClassDecl node() {
        return (ClassDecl) super.node();
    }

    @Override
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
        superLang().prettyPrintHeader(node(), w, tr);
    }

    @Override
    public void prettyPrintFooter(CodeWriter w, PrettyPrinter tr) {
        superLang().prettyPrintFooter(node(), w, tr);
    }

    @Override
    public Node addDefaultConstructor(TypeSystem ts, NodeFactory nf,
            ConstructorInstance defaultConstructorInstance)
            throws SemanticException {
        return node();
    }

    public ClassDecl collectEFMethodInfo(EFGInfoCollector v) {
        EfgTypeSystem ts = v.typeSystem();
        ClassDecl cd = node();

        // Ignore classes.
        if (!cd.flags().isInterface()) {
            return cd;
        }

        // Ignore interfaces that do not extend Node.
        if (!cd.type().isSubtype(ts.Node())) {
            return cd;
        }

        v.addClass(cd);

        return cd;
    }

}
