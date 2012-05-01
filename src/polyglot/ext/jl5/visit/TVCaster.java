package polyglot.ext.jl5.visit;

import polyglot.ast.*;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;

public class TVCaster extends AscriptionVisitor {
    private final boolean promiscuousMode;
    public TVCaster(Job job, TypeSystem ts, NodeFactory nf) {
        this(job, ts, nf, false);
    }

    public TVCaster(Job job, TypeSystem ts, NodeFactory nf, boolean promiscuousMode) {
        super(job, ts, nf);
        this.promiscuousMode = promiscuousMode;
    }

    @Override
    public Expr ascribe(Expr e, Type toType) throws SemanticException {
        if (e.type() == null || toType == null || !toType.isCanonical()) {
            return e;
        }
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        Type fromType = ts.erasureType(e.type());
        toType = ts.erasureType(toType);
        if (!fromType.isReference() || !toType.isReference() || ts.Object().equals(toType)) {
            return e;
        }
        if (e instanceof Special || e instanceof ArrayInit || e instanceof Lit) {
            return e;
        }
        
        if (ts.isCastValid(fromType, toType) && (promiscuousMode || !ts.isImplicitCastValid(fromType, toType))) {            
            return insertCast(e, toType);
        }
        return e;
    }

    private Expr insertCast(Expr e, Type toType) throws SemanticException {
        if (toType.isClass() && toType.toClass().fullName().equals("java.lang.Enum")) {
            // it's the enum type.
            // see if we want to replace it
            JL5Options opts = (JL5Options) job.extensionInfo().getOptions();
            String enumImpl = opts.enumImplClass;
            if (opts.removeJava5isms && enumImpl != null) {
                toType = ts.typeForName(enumImpl);
            }
        }
        TypeNode tn = nf.CanonicalTypeNode(Position.compilerGenerated(), toType);
        Expr newE = nf.Cast(Position.compilerGenerated(), tn, e);
        return newE.type(toType);            
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v) throws SemanticException {
        Node ret = super.leaveCall(parent, old, n, v);
        if (parent instanceof Eval && ret instanceof Cast) {
            // inserted one cast too many...
            Cast c = (Cast) ret;
            return c.expr();
        }
        if (parent instanceof Assign && ret instanceof Cast && ((Assign)parent).left() == old) {
            // inserted one cast too many...
            Cast c = (Cast) ret;
            return c.expr();
        }
        if (parent instanceof Throw && ret instanceof Cast) {
            // let's make sure we don't change an unchecked exception to a checked exception
            Cast c = (Cast) ret;
            c = c.castType(c.castType().type(c.expr().type()));
            c = (Cast) c.type(c.expr().type());
            ret = c;
        }
        if (parent instanceof Conditional) {
            Conditional c = (Conditional)parent;
            if (c.consequent() == old || c.alternative() == old) {
                // n is the consequent or alternative
                if (c.type().isReference() && !((Expr)n).type().equals(c.type())) {
                    // c is a reference type that's different from the type of the conditional.
                    // add a cast, since the Java 1.5 typing rules for conditionals are more permissive.
                    return insertCast((Expr)n, c.type());
                }
            }
        }
        if (parent instanceof Call && old == ((Call)parent).target()) {
            Call c = (Call)parent;
            if (c.target() instanceof Expr && !(c.target() instanceof Special)) {
                Expr e = (Expr)n;
                if (e instanceof Cast) {
                    e = ((Cast)e).expr();
                }
                // cast e to the type of the container
                JL5TypeSystem ts = (JL5TypeSystem)this.ts;
                Type t = c.methodInstance().container();
                Type et = ts.erasureType(t);
                return insertCast(e, et);
            }
            
        }
        return ret;
    }
    
    

}
