package polyglot.ext.jl5.visit;

import java.util.List;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.JL5Import;
import polyglot.ext.jl5.types.JL5ImportTable;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.StringUtil;
import polyglot.visit.InitImportsVisitor;
import polyglot.visit.NodeVisitor;

/** Visitor which traverses the AST constructing type objects. */
public class JL5InitImportsVisitor extends InitImportsVisitor {
    public JL5InitImportsVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        Node r = super.leaveCall(old, n, v);
        if (r instanceof Import) {
            JL5Import im = (JL5Import) n;
            JL5ImportTable it = (JL5ImportTable) this.importTable;

            if (im.kind() == JL5Import.SINGLE_STATIC_MEMBER) {
                String id = StringUtil.getShortNameComponent(im.name());
                checkForConflicts(id, it.classImports(), im.position());
                it.addSingleStaticImport(im.name(), im.position());
            }
            else if (im.kind() == JL5Import.STATIC_ON_DEMAND) {
                it.addStaticOnDemandImport(im.name(), im.position());
            }
            else if (im.kind() == Import.CLASS) {
                // just check for conflicts
                String id = StringUtil.getShortNameComponent(im.name());
                checkForConflicts(id, it.singleStaticImports(), im.position());
            }
        }

        return r;
    }

    private void checkForConflicts(String id, List<String> imports,
            Position position) throws SemanticException {
        for (String other : imports) {
            String name = StringUtil.getShortNameComponent(other);
            if (id.equals(name)) {
                throw new SemanticException("The import statement "
                                                    + this
                                                    + " collides with import statement "
                                                    + other + " .",
                                            position);
            }
        }

    }
}
