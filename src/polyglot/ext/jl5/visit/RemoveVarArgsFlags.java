package polyglot.ext.jl5.visit;

import polyglot.ast.ConstructorDecl;
import polyglot.ast.Formal;
import polyglot.ast.LocalDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;

public class RemoveVarArgsFlags extends NodeVisitor {

    public RemoveVarArgsFlags(Job job, TypeSystem ts, NodeFactory nf) {
        super();
    }

    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        if (n instanceof MethodDecl) {
            MethodDecl d = (MethodDecl) n;
            return d.flags(JL5Flags.clearVarArgs(d.flags()));
        }
        if (n instanceof ConstructorDecl) {
            ConstructorDecl d = (ConstructorDecl) n;
            return d.flags(JL5Flags.clearVarArgs(d.flags()));
        }
        if (n instanceof Formal) {
            Formal d = (Formal) n;
            return d.flags(JL5Flags.clearVarArgs(d.flags()));
        }
        if (n instanceof LocalDecl) {
            LocalDecl d = (LocalDecl) n;
            return d.flags(JL5Flags.clearVarArgs(d.flags()));
        }
        return n;
    }

}
