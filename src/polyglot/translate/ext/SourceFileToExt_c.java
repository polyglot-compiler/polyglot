package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.frontend.Source;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class SourceFileToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        SourceFile n = (SourceFile) node();
        Source source = n.source();
        n =
                rw.to_nf().SourceFile(n.position(),
                                      n.package_(),
                                      n.imports(),
                                      n.decls());
        n = n.source(source);
        return n;
    }
}
