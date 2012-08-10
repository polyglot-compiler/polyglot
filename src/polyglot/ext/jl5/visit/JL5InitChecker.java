package polyglot.ext.jl5.visit;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.InitChecker;
import polyglot.visit.NodeVisitor;

public class JL5InitChecker extends InitChecker {
    public JL5InitChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        if (n instanceof ClassBody) {
            // we are starting to process a class declaration, but have yet
            // to do any of the dataflow analysis.

            // set up the new ClassBodyInfo, and make sure that it forms
            // a stack.
            ClassType ct = null;
            if (parent instanceof ClassDecl) {
                ct = ((ClassDecl) parent).type();
            }
            else if (parent instanceof New) {
                ct = ((New) parent).anonType();
            }
            else if (parent instanceof EnumConstantDecl) {
                ct = ((EnumConstantDecl) parent).type();
            }
            if (ct == null) {
                throw new InternalCompilerError("ClassBody found but cannot find the class.",
                                                n.position());
            }
            setupClassBody(ct, (ClassBody) n);
        }

        return super.enterCall(n);
    }

}
