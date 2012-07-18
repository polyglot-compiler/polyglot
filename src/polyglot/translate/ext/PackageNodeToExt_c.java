package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.PackageNode;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.Package;
import polyglot.types.SemanticException;

public class PackageNodeToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        PackageNode n = (PackageNode) node();
        Package p = n.package_();
        p = rw.to_ts().packageForName(p.fullName());
        return rw.to_nf().PackageNode(n.position(), p);
    }
}
