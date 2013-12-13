package polyglot.ext.jl7.ast;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.Node;
import polyglot.ast.Throw;
import polyglot.ast.Try;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyglot.util.SubtypeSet;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;

public class JL7TryExt extends JL7Ext implements JL7TryOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Block exceptionCheckTryBlock(ExceptionChecker ec)
            throws SemanticException {
        Block b =
                this.superDel().TryOps(this.node()).exceptionCheckTryBlock(ec);

        ((JL7Del) this.node().del()).TryOps(this.node())
                                    .checkPreciseRethrows(ec.typeSystem(), b);

        return b;
    }

    @Override
    public ExceptionChecker constructTryBlockExceptionChecker(
            ExceptionChecker ec) {
        return this.superDel()
                   .TryOps(this.node())
                   .constructTryBlockExceptionChecker(ec);
    }

    @Override
    public List<Catch> exceptionCheckCatchBlocks(ExceptionChecker ec)
            throws SemanticException {
        return this.superDel()
                   .TryOps(this.node())
                   .exceptionCheckCatchBlocks(ec);
    }

    @Override
    public Block exceptionCheckFinallyBlock(ExceptionChecker ec)
            throws SemanticException {
        return this.superDel()
                   .TryOps(this.node())
                   .exceptionCheckFinallyBlock(ec);
    }

    @Override
    public void checkPreciseRethrows(TypeSystem ts, Block tryBlock) {
        Try n = (Try) this.node();

        // For each catch block, identify which exceptions can get to it.
        // First, get the set of all exceptions that the try block can throw
        SubtypeSet thrown =
                new SubtypeSet(ts.Throwable(), tryBlock.throwTypes(ts));

        // Second, go through the catch blocks, and see what exceptions can actually reach them.
        for (Catch cb : n.catchBlocks()) {
            Type catchType = cb.catchType();

            // The exceptions that can reach cb are the exceptions in thrown
            // that may be assignable to catchType.

            ((JL7Del) this.node().del()).TryOps(this.node())
                                        .preciseRethrowsForCatchBlock(cb,
                                                                      thrown);

            thrown.remove(catchType);
        }

    }

    @Override
    public void preciseRethrowsForCatchBlock(Catch cb, SubtypeSet reaching) {
        List<Type> s = new ArrayList<Type>();
        for (Type t : reaching) {
            if (cb.catchType().isSubtype(t)) {
                // nope, it's not worth it.
                // No precision to be gained.
                return;
            }
            if (t.isSubtype(cb.catchType())) {
                s.add(t);
            }
        }
        // now, if cb.formal() is final, or effectively final, then
        // set the throwsSet of any rethrow.
        if (isFinalFormal(cb)) {
            setThrowsTypes(cb.formal().localInstance(), s, cb.body());
        }
    }

    protected boolean isFinalFormal(Catch cb) {
        if (cb.formal().localInstance().flags().isFinal()
                || cb instanceof MultiCatch) {
            // explcitly final
            return true;
        }
        // Check to see if the local is effectively final.
        EffectivelyFinalVisitor v =
                new EffectivelyFinalVisitor(cb.formal().localInstance());
        cb.body().visit(v);

        return v.isEffectivelyFinal();
    }

    public class EffectivelyFinalVisitor extends NodeVisitor {
        boolean isEffectivelyFinal;
        LocalInstance li;

        public EffectivelyFinalVisitor(LocalInstance li) {
            this.li = li;
            this.isEffectivelyFinal = true;
        }

        public boolean isEffectivelyFinal() {
            return this.isEffectivelyFinal;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof LocalAssign) {
                LocalAssign la = (LocalAssign) n;
                if (((Local) la.left()).localInstance().equals(li)) {
                    this.isEffectivelyFinal = false;
                }
            }
            return n;
        }

    }

    protected void setThrowsTypes(LocalInstance localInstance, List<Type> s,
            Block b) {
        SetThrowSetVisitor v = new SetThrowSetVisitor(localInstance, s);
        b.visit(v);

    }

    public class SetThrowSetVisitor extends NodeVisitor {
        LocalInstance li;
        List<Type> s;

        public SetThrowSetVisitor(LocalInstance li, List<Type> s) {
            this.li = li;
            this.s = s;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Throw) {
                Throw t = (Throw) n;
                if (t.expr() instanceof Local) {
                    Local l = (Local) t.expr();
                    if (l.localInstance().equals(this.li)) {
                        // set the throw set.
                        JL7ThrowExt ext = (JL7ThrowExt) JL7Ext.ext(t);
                        ext.throwSet = s;
                    }
                }
            }
            return n;
        }
    }
}
