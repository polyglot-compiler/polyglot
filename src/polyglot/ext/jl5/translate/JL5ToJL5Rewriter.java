package polyglot.ext.jl5.translate;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.ext.jl5.types.IntersectionType;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.WildCardType;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class JL5ToJL5Rewriter extends ExtensionRewriter {

    public JL5ToJL5Rewriter(Job job, ExtensionInfo from_ext,
            ExtensionInfo to_ext) {
        super(job, from_ext, to_ext);
    }

    @Override
    public TypeNode typeToJava(Type t, Position pos) {
        JL5NodeFactory to_nf = (JL5NodeFactory) to_nf();
        // JL5ToJL5Rewriter.typeToJava
        if (t instanceof IntersectionType) {
            IntersectionType ct = (IntersectionType) t.toClass();
            List<TypeNode> bounds = new ArrayList<>(ct.bounds().size());
            for (ReferenceType rt : ct.bounds())
                bounds.add(typeToJava(rt, pos));
            return to_nf.ParamTypeNode(pos, to_nf.Id(pos, ct.name()), bounds);

        }
        else if (t instanceof LubType) {
            throw new InternalCompilerError("I don't understand what to do here");

        }
        else if (t instanceof WildCardType) {
            WildCardType wc = (WildCardType) t;
            if (wc.isSuperConstraint()) {
                TypeNode superNode = typeToJava(wc.lowerBound(), pos);
                return to_nf.AmbWildCardSuper(pos, superNode);
            }
            else if (wc.isExtendsConstraint()) {
                TypeNode extendsNode = typeToJava(wc.upperBound(), pos);
                return to_nf.AmbWildCardExtends(pos, extendsNode);
            }
            else {
                return to_nf.AmbWildCard(pos);
            }
        }
        else if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) t;
            return to_nf.AmbTypeNode(pos, to_nf.Id(pos, tv.name()));
        }

        if (t.isClass()) {
            if (t instanceof JL5ParsedClassType) {
                JL5ParsedClassType ct = (JL5ParsedClassType) t.toClass();
                List<TypeNode> tvs = new ArrayList<>(ct.typeVariables().size());
                for (ReferenceType rt : ct.typeVariables())
                    tvs.add(typeToJava(rt, pos));
                return to_nf.TypeNodeFromQualifiedName(pos, ct.fullName(), tvs);
            }
            else if (t instanceof JL5SubstClassType) {
                JL5SubstClassType ct = (JL5SubstClassType) t.toClass();
                List<TypeNode> actuals = new ArrayList<>(ct.actuals().size());
                for (ReferenceType rt : ct.actuals())
                    actuals.add(typeToJava(rt, pos));

                return to_nf.TypeNodeFromQualifiedName(pos,
                                                       ct.fullName(),
                                                       actuals);
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
