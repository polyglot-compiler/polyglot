package polyglot.ext.jl5.ast;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Ambiguous;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class AmbTypeInstantiation extends TypeNode_c implements TypeNode,
        Ambiguous {

    private TypeNode base;
    private List<TypeNode> typeArguments;

    public AmbTypeInstantiation(Position pos, TypeNode base,
            List<TypeNode> typeArguments) {
        super(pos);
        this.base = base;
        if (typeArguments == null) {
            typeArguments = Collections.emptyList();
        }
        this.typeArguments = typeArguments;
    }

    @Override
    public String name() {
        return base.name();
    }

    @Override
    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        if (!this.base.isDisambiguated()) {
            return this;
        }
        for (TypeNode tn : typeArguments) {
            if (!tn.isDisambiguated()) {
                return this;
            }
        }

        JL5TypeSystem ts = (JL5TypeSystem) sc.typeSystem();
        Type baseType = this.base.type();
//        System.err.println("Base type is " + base);
//        System.err.println("typeArguments is " + typeArguments);
//        if (baseType instanceof JL5SubstClassType) {
//            System.err.println("  " + this.position);
//            System.err.println("    base type of " + this + " is " + base.type()+ " " + base.type().getClass());
//            System.err.println("   base type base is " + ((JL5SubstClassType)baseType).base());
//            System.err.println("   base type instantiation is " + ((JL5SubstClassType)baseType).subst() + "  " + ((JL5SubstClassType)baseType).subst().getClass());
//            System.err.println("   type args are is " + this.typeArguments);
//        }

        if (baseType instanceof ClassType) {
            ClassType ct = (ClassType) baseType;
            if (ct.isInnerClass()) {
                if (ct.outer() instanceof RawClass && !(ct instanceof RawClass)) {
                    // we are trying to create a "rare" class!
                    // That is, we are
                    // trying to instantiate a member class of
                    // a raw class.
                    // See JLS 3rd ed. 4.8
                    throw new SemanticException("\"Rare\" types are not allowed: cannot provide "
                                                        + "type arguments to member class "
                                                        + ct.name()
                                                        + " of raw class "
                                                        + ct.outer() + ".",
                                                position);
                }
            }
        }

        JL5ParsedClassType pct;
        Map<TypeVariable, ReferenceType> typeMap =
                new LinkedHashMap<TypeVariable, ReferenceType>();
        if (baseType instanceof JL5ParsedClassType) {
            pct = (JL5ParsedClassType) baseType;
        }
        else if (baseType instanceof RawClass) {
            pct = ((RawClass) baseType).base();
        }
        else if (baseType instanceof JL5SubstClassType) {
            JL5SubstClassType sct = (JL5SubstClassType) baseType;
            pct = sct.base();
            Iterator<Map.Entry<TypeVariable, ReferenceType>> iter =
                    sct.subst().entries();
            while (iter.hasNext()) {
                Map.Entry<TypeVariable, ReferenceType> e = iter.next();
                typeMap.put(e.getKey(), e.getValue());
            }
        }
        else {
            throw new InternalCompilerError("Don't know how to handle "
                    + baseType, position);
        }

        if ((pct.pclass() == null || pct.pclass().formals().isEmpty())) {
            if (this.typeArguments.isEmpty()) {
                // the base class has no formals, and no actuals were supplied.
                return base;
            }
        }

        if (pct.pclass().formals().size() != this.typeArguments.size()) {
            throw new SemanticException("Wrong number of type parameters for class "
                                                + pct,
                                        this.position);
        }

        // if subst is not null, check that subst does not already define the formal type variables
        if (!typeMap.isEmpty()) {
            if (typeMap.keySet().containsAll(pct.typeVariables())) {
                throw new SemanticException("Cannot instantiate " + baseType
                        + " with arguments " + typeArguments, this.position());
            }
        }

        // add the new mappings 
        List<TypeVariable> formals = pct.typeVariables();
        for (int i = 0; i < formals.size(); i++) {
            ReferenceType t = (ReferenceType) typeArguments.get(i).type();
            typeMap.put(formals.get(i), t);
        }

//        System.err.println("Instantiating " + pct + " with " + actuals);
        Type instantiated = ts.subst(pct, typeMap);
        return sc.nodeFactory().CanonicalTypeNode(this.position, instantiated);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        base.prettyPrint(w, tr);
        w.write("<");
        Iterator<TypeNode> iter = typeArguments.iterator();
        while (iter.hasNext()) {
            TypeNode tn = iter.next();
            tn.prettyPrint(w, tr);

            if (iter.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }
        w.write(">");
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode base = (TypeNode) visitChild(this.base, v);
        List<TypeNode> arguments = visitList(this.typeArguments, v);
        return this.base(base).typeArguments(arguments);
    }

    private AmbTypeInstantiation typeArguments(List<TypeNode> arguments) {
        if (this.typeArguments == arguments) {
            return this;
        }
        AmbTypeInstantiation n = (AmbTypeInstantiation) this.copy();
        n.typeArguments = arguments;
        return n;
    }

    private AmbTypeInstantiation base(TypeNode b) {
        if (this.base == b) {
            return this;
        }
        AmbTypeInstantiation n = (AmbTypeInstantiation) this.copy();
        n.base = b;
        return n;
    }
}
