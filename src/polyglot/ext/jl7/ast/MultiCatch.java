package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.Catch;
import polyglot.ast.TypeNode;

public interface MultiCatch extends Catch {
    List<TypeNode> alternatives();

    MultiCatch alternatives(List<TypeNode> alternatives);

}
