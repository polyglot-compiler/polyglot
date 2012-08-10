package polyglot.ext.jl5.visit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.ErrorHandlingVisitor;

/**
 * This class rewrites method decls to change the type of arguments and the return value
 * so that the appropriate override relationships will hold in Java 1.4.
 */
public class TypeErasureProcDecls extends ErrorHandlingVisitor {
    public TypeErasureProcDecls(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node n) throws SemanticException {
        if (n instanceof MethodDecl) {
            return rewriteMethodDecl((MethodDecl) n);
        }
        return super.leaveCall(n);
    }

    private Node rewriteMethodDecl(MethodDecl n) {
        // find the instance that it overrides
        MethodInstance mi = n.methodInstance();
        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();

        List<? extends MethodInstance> implemented = mi.implemented();
        if (implemented.isEmpty()) {
            // doesn't implement anything
            return n;
        }
        // get the last element, i.e., from the most superest class.
        MethodInstance mj = implemented.get(implemented.size() - 1);
        if (mj == mi) {
            // doesn't implement anything
            return n;
        }

        JL5ClassType miContainer = (JL5ClassType) mi.container();
        List<? extends Type> miFormalTypes = mi.formalTypes();
        if (miContainer instanceof JL5ParsedClassType) {
            JL5ParsedClassType pct = (JL5ParsedClassType) miContainer;
            JL5Subst es = pct.erasureSubst();
            if (es != null) {
                miFormalTypes = es.substTypeList(miFormalTypes);
            }
        }
        ReferenceType erasedMjContainer =
                (ReferenceType) ts.erasureType(mj.container());
        MethodInstance mjErased;
        try {
            mjErased =
                    ts.findMethod(erasedMjContainer,
                                  mi.name(),
                                  miFormalTypes,
                                  erasedMjContainer.toClass());
        }
        catch (SemanticException e) {
            // hmmm couldn't find the correct method
            throw new InternalCompilerError("Couldn't find erased version of "
                    + mj + " in " + erasedMjContainer + " with name "
                    + mi.name() + " and args " + miFormalTypes + ". "
                    + erasedMjContainer.methods(), e);
        }

        // we need to rewrite the method decl to have the same arguments as mjErased, the erased version of mj.
        boolean changed = false;
        List<Formal> newFormals = new ArrayList<Formal>(n.formals().size());
        Iterator<Formal> formals = n.formals().iterator();
        for (Type tj : mjErased.formalTypes()) {
            Formal f = formals.next();
            TypeNode tn = f.type();
            TypeNode newTn = tn.type(ts.erasureType(tj));
            changed = changed || (tn != newTn);
            newFormals.add(f.type(newTn));
        }

        // also change the return type, so Java 1.4 won't complain
        TypeNode retType = n.returnType();
        TypeNode newRetType = retType.type(mjErased.returnType());
        changed = changed || (retType != newRetType);

        if (!changed) {
            return n;
        }
        return n.formals(newFormals).returnType(newRetType);
    }

}
