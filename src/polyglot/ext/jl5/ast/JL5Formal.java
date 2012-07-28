package polyglot.ext.jl5.ast;

import polyglot.ast.Formal;

public interface JL5Formal extends Formal, AnnotatedElement {
    boolean isVarArg();
}
