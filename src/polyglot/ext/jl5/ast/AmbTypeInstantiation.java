package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Ambiguous;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class AmbTypeInstantiation extends TypeNode_c implements TypeNode, Ambiguous {

    private TypeNode base;
    private List<TypeNode> typeArguments;
    public AmbTypeInstantiation(Position pos, TypeNode base, List typeArguments) {
        super(pos);
        this.base = base;
        if (typeArguments == null) {
            typeArguments = Collections.EMPTY_LIST;
        }
        this.typeArguments = typeArguments;
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

        JL5TypeSystem ts = (JL5TypeSystem)sc.typeSystem();
//        System.err.println("Base type is " + base);
//        System.err.println("typeArguments is " + typeArguments);
        Type baseType = this.base.type();
        if (baseType instanceof JL5SubstClassType) {
            System.err.println("" + this.position);
            System.err.println("base type of " + this + " is " + base.type()+ " " + base.type().getClass());
            System.err.println(" base type base is " + ((JL5SubstClassType)baseType).base());
            System.err.println(" base type instantiation is is " + ((JL5SubstClassType)baseType).subst());
            System.err.println(" type args are is " + this.typeArguments);
        }
        // SC: hack! Not sure what's going on here...
        while (baseType instanceof JL5SubstClassType) {
            baseType = ((JL5SubstClassType)baseType).base();
        }
        
        JL5ParsedClassType pct;
        if (baseType instanceof JL5ParsedClassType) {
            pct = (JL5ParsedClassType)baseType;
        }
        else if (baseType instanceof RawClass) {
            pct = ((RawClass)baseType).base();
        }
        else {
            throw new InternalCompilerError("Don't know how to handle " + baseType, position);
        }

        if ((pct.pclass() == null || pct.pclass().formals().isEmpty())) {
            if (this.typeArguments.isEmpty()) {
                // the base class has no formals, and no actuals were supplied.
                return base;
            }
        }
        
        if (pct.pclass().formals().size() != this.typeArguments.size()) {
            throw new SemanticException("Wrong number of type parameters for class " + pct, this.position);
        }

        
        List<Type> actuals = new ArrayList(typeArguments.size());
        for (TypeNode tn : typeArguments) {
            actuals.add(tn.type());
        }
        
//        System.err.println("Instantiating " + pct + " with " + actuals);
        Type instantiated = ts.instantiate(position, pct.pclass(), actuals);
        return sc.nodeFactory().CanonicalTypeNode(this.position, instantiated);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        base.prettyPrint(w, tr);
        w.write("<");
        Iterator iter = typeArguments.iterator();
        while (iter.hasNext() ) {
            TypeNode tn = (TypeNode)iter.next();             
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
        TypeNode base = (TypeNode)visitChild(this.base, v);
        List arguments = visitList(this.typeArguments, v);
        return this.base(base).typeArguments(arguments);
    }


    private AmbTypeInstantiation typeArguments(List arguments) {
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
