package polyglot.ext.jl5.translate;

import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;

public class JL5ToJLRewriter extends ExtensionRewriter {
    protected final JL5TypeSystem jl5ts;

    public JL5ToJLRewriter(Job job, ExtensionInfo from_ext, ExtensionInfo to_ext) {
        super(job, from_ext, to_ext);
        jl5ts = (JL5TypeSystem) from_ext.typeSystem();
    }

    @Override
    public TypeNode typeToJava(Type t, Position pos) throws SemanticException {
        // TODO: disentangle erasureType from translated type
        t = jl5ts.erasureType(t);
        if (t instanceof LubType) t = ((LubType) t).calculateLub();
        return super.typeToJava(t, pos);
    }

}
