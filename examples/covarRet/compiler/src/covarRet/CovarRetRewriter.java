package covarRet;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;

/**
 * This visitor rewrites the AST to translate from Java with covariant returns
 * to standard Java.
 */
public class CovarRetRewriter extends AscriptionVisitor {
    public CovarRetRewriter(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Expr ascribe(Expr e, Type toType) {
        if (e instanceof Call) {
            /* Add a cast to the appropriate subclass, if necessary */
            Call c = (Call) e;

            Position p = c.position();

            // Insert a cast
            MethodInstance mi = c.methodInstance();
            Type overridenRetType = getOverridenReturnType(mi);

            if (overridenRetType != null
                    && !ts.isImplicitCastValid(overridenRetType, toType)) {

                // The overriden return type cannot be implicitly cast to the
                // expected type, so explicitly cast it.
                NodeFactory nf = nodeFactory();
                return nf.Cast(p, nf.CanonicalTypeNode(p, mi.returnType()), c);
            }
        }

        return e;
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {

        if (n instanceof MethodDecl) {
            // Change the return type of the overridden method
            // to be the same as the superclass's
            MethodDecl md = (MethodDecl) n;
            Position p = md.position();

            MethodInstance mi = md.methodInstance();
            Type overridenRetType = getOverridenReturnType(mi);
            if (overridenRetType != null) {
                NodeFactory nf = nodeFactory();
                return md.returnType(nf.CanonicalTypeNode(p, overridenRetType));
            }
        }

        return super.leaveCall(old, n, v);
    }

    /**
     * Get the return type of the method that mi overrides if
     * that return type varies from the return type of the method mi.
     * Return null otherwise.
     */
    private Type getOverridenReturnType(MethodInstance mi) {
        Type retType = null;

        for (MethodInstance mj : ts.overrides(mi)) {
            if (!ts.isAccessible(mj, this.context)) {
                break;
            }

            if (ts.isSubtype(mi.returnType(), mj.returnType())
                    && !ts.equals(mi.returnType(), mj.returnType())) {
                // mj.returnType() is the type to use!
                retType = mj.returnType();
            }
        }

        return retType;
    }
}
