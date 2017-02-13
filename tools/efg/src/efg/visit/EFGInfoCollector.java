package efg.visit;

import static efg.ExtensionInfo.EFG_INFO;

import efg.ExtensionInfo;
import efg.ast.EfgClassDeclExt;
import efg.ast.EfgExt;
import efg.ast.EfgNodeFactory;
import efg.types.EfgTypeSystem;
import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.visit.ErrorHandlingVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Collects information about extension methods that need to be generated.
 */
public class EFGInfoCollector extends ErrorHandlingVisitor {

    protected final ExtensionInfo extInfo;
    protected final EfgTypeSystem ts;

    public EFGInfoCollector(Job job, ExtensionInfo extInfo, EfgTypeSystem ts,
            EfgNodeFactory nf) {
        super(job, ts, nf);
        this.extInfo = extInfo;
        this.ts = ts;
    }

    @Override
    public EfgTypeSystem typeSystem() {
        return ts;
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        EFG_INFO.ensureConfig(extInfo);

        EfgExt ext = EfgExt.ext(n);
        if (ext instanceof EfgClassDeclExt) {
            return ((EfgClassDeclExt) ext).collectEFMethodInfo((EFGInfoCollector) v);
        }

        return n;
    }

    /**
     * Adds auto-generated method basenames for the given class.
     */
    public void addClass(ClassDecl cd) {
        EFG_INFO.addClass(extInfo, cd);
    }
}
