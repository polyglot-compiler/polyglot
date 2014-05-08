package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.TryOps;
import polyglot.ext.jl5.ast.JL5Del;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyglot.util.SubtypeSet;
import polyglot.visit.ExceptionChecker;

public class JL7TryDel extends JL5Del implements TryOps, JL7TryOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Block exceptionCheckTryBlock(ExceptionChecker ec)
            throws SemanticException {
        return ((JL7TryExt) JL7Ext.ext(this.node())).exceptionCheckTryBlock(ec);
    }

    @Override
    public ExceptionChecker constructTryBlockExceptionChecker(
            ExceptionChecker ec) {
        return ((TryOps) this.node()).constructTryBlockExceptionChecker(ec);
    }

    @Override
    public List<Catch> exceptionCheckCatchBlocks(ExceptionChecker ec)
            throws SemanticException {
        return ((TryOps) this.node()).exceptionCheckCatchBlocks(ec);
    }

    @Override
    public Block exceptionCheckFinallyBlock(ExceptionChecker ec)
            throws SemanticException {
        return ((TryOps) this.node()).exceptionCheckFinallyBlock(ec);
    }

    @Override
    public void checkPreciseRethrows(TypeSystem ts, Block tryBlock) {
        ((JL7TryExt) JL7Ext.ext(this.node())).checkPreciseRethrows(ts, tryBlock);
    }

    @Override
    public void preciseRethrowsForCatchBlock(Catch cb, SubtypeSet reaching) {
        ((JL7TryExt) JL7Ext.ext(this.node())).preciseRethrowsForCatchBlock(cb,
                                                                           reaching);

    }

}
