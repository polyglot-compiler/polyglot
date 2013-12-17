package polyglot.ext.jl7.ast;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.JLang;
import polyglot.ast.TryOps;
import polyglot.types.TypeSystem;
import polyglot.util.SubtypeSet;

public interface JL7TryOps extends TryOps {

    void checkPreciseRethrows(JLang lang, TypeSystem typeSystem, Block b);

    void preciseRethrowsForCatchBlock(JLang lang, Catch cb, SubtypeSet thrown);

}
