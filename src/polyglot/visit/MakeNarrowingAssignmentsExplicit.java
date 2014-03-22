package polyglot.visit;

import polyglot.ast.Assign;
import polyglot.ast.Cast;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.Type;
import polyglot.util.Position;

/*
 * Make primitive narrowing assignment conversions explicit (JLS 2nd ed. 5.2).
 */
public class MakeNarrowingAssignmentsExplicit extends NodeVisitor {

    private NodeFactory nf;

    public MakeNarrowingAssignmentsExplicit(NodeFactory nf) {
        super(nf.lang());
        this.nf = nf;
    }

    private Expr rewriteRHS(Type toType, Expr r) {
        Type rt = r.type();
        if (lang().isConstant(r, lang()) && rt.isIntOrLess()
                && (toType.isByte() || toType.isShort() || toType.isChar())) {
            // Assume that this was the result of a narrowing primitive conversion
            // and make it explicit by adding a cast.
            Position pos = r.position();
            Cast c =
                    (Cast) nf.Cast(pos, nf.CanonicalTypeNode(pos, toType), r)
                             .type(toType);
            return c;
        }
        return r;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof FieldDecl && ((FieldDecl) n).init() != null) {
            FieldDecl fd = (FieldDecl) n;
            return fd.init(rewriteRHS(fd.declType(), fd.init()));
        }
        if (n instanceof LocalDecl && ((LocalDecl) n).init() != null) {
            LocalDecl ld = (LocalDecl) n;
            return ld.init(rewriteRHS(ld.declType(), ld.init()));
        }
        if (n instanceof Assign
                && Assign.ASSIGN.equals(((Assign) n).operator())) {
            Assign a = (Assign) n;
            return a.right(rewriteRHS(a.left().type(), a.right()));
        }
        return n;
    }
}
