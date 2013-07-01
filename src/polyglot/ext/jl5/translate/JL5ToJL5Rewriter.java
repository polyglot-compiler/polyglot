package polyglot.ext.jl5.translate;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.ext.jl5.types.IntersectionType;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class JL5ToJL5Rewriter extends ExtensionRewriter {

    public JL5ToJL5Rewriter(Job job, ExtensionInfo from_ext,
            ExtensionInfo to_ext) {
        super(job, from_ext, to_ext);
    }

    @Override
    public TypeNode typeToJava(Type t, Position pos) throws SemanticException {
        if (t.isClass()) {
            JL5NodeFactory to_nf = (JL5NodeFactory) to_nf();
            if (t instanceof JL5ParsedClassType) {
                JL5ParsedClassType ct = (JL5ParsedClassType) t.toClass();
//                List<TypeNode> typeArgs =
//                        new ArrayList<TypeNode>(ct.typeVariables().size());
//                for (TypeVariable tv : ct.typeVariables())
//                    typeArgs.add(typeToJava(tv, pos));
                if (!ct.typeVariables().isEmpty())
                    throw new InternalCompilerError("I don't understand what to do here");
                return to_nf.TypeNodeFromQualifiedName(pos, ct.fullName());
            }
            else if (t instanceof IntersectionType) {
                IntersectionType ct = (IntersectionType) t.toClass();
                List<TypeNode> bounds =
                        new ArrayList<TypeNode>(ct.bounds().size());
                for (ReferenceType rt : ct.bounds())
                    bounds.add(typeToJava(rt, pos));
                return to_nf.ParamTypeNode(pos,
                                           bounds,
                                           to_nf.Id(pos, ct.name()));
            }
            else if (t instanceof JL5SubstClassType) {
                JL5SubstClassType ct = (JL5SubstClassType) t.toClass();
                List<TypeNode> actuals =
                        new ArrayList<TypeNode>(ct.actuals().size());
                for (ReferenceType rt : ct.actuals())
                    actuals.add(typeToJava(rt, pos));

                return to_nf.TypeNodeFromQualifiedName(pos,
                                                       ct.fullName(),
                                                       actuals);
            }
            else if (t instanceof LubType) {
                throw new InternalCompilerError("I don't understand what to do here");
            }
            else if (t instanceof RawClass) {
                return to_nf.TypeNodeFromQualifiedName(pos, t.toClass()
                                                             .fullName());
            }
            else {
                throw new InternalCompilerError("Unknown class type: "
                        + t.getClass());
            }
        }
        return super.typeToJava(t, pos);
    }
}
