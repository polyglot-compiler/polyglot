package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.Job;
import java.util.*;

/** Visitor which performs type checking on the AST. */
public class AscriptionVisitor extends ContextVisitor
{
    Type type;
    AscriptionVisitor outer;

    public AscriptionVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        type = null;
        outer = null;
    }

    public AscriptionVisitor pop() {
        return outer;
    }

    public Type toType() {
        return type;
    }

    public NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        Type t = null;

        if (parent != null && n instanceof Expr) {
            t = parent.childExpectedType((Expr) n, this);
        }

        AscriptionVisitor v = (AscriptionVisitor) copy();
        v.outer = this;
        v.type = t;

        return v;
    }

    public Expr ascribe(Expr e, Type toType) throws SemanticException {
        return e;
    }

    public Node leaveCall(Node old, Node n, NodeVisitor v)
        throws SemanticException {

        if (n instanceof Expr) {
            Expr e = (Expr) n;
            Type type = ((AscriptionVisitor) v).type;
            return ascribe(e, type);
        }

	return n;
    }
}
