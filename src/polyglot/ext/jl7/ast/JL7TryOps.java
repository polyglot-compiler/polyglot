package polyglot.ext.jl7.ast;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.types.TypeSystem;
import polyglot.util.SubtypeSet;

public interface JL7TryOps {

    void checkPreciseRethrows(TypeSystem typeSystem, Block b);

    void preciseRethrowsForCatchBlock(Catch cb, SubtypeSet thrown);

}
