package polyglot.ext.jl7.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Ambiguous;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class AmbUnionType extends TypeNode_c implements TypeNode, Ambiguous {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private List<TypeNode> alternatives;

    public AmbUnionType(Position pos, List<TypeNode> alternatives) {
        super(pos);
        this.alternatives = alternatives;
    }

    @Override
    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        List<ReferenceType> types =
                new ArrayList<ReferenceType>(this.alternatives.size());
        for (TypeNode tn : this.alternatives) {
            if (!tn.isDisambiguated()) {
                return this;
            }
            types.add((ReferenceType) tn.type());
        }

        JL7TypeSystem ts = (JL7TypeSystem) sc.typeSystem();
        Type t;
        if (types.size() == 1) {
            t = types.get(0);
        }
        else {
            t = ts.lub(this.position(), types);
        }
        return sc.nodeFactory().CanonicalTypeNode(this.position, t);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        Iterator<TypeNode> i = this.alternatives.iterator();
        while (i.hasNext()) {
            TypeNode a = i.next();
            a.prettyPrint(w, tr);
            if (i.hasNext()) {
                w.write("|");
            }
        }
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<TypeNode> newAlts =
                new ArrayList<TypeNode>(this.alternatives.size());
        for (TypeNode tn : this.alternatives) {
            newAlts.add((TypeNode) visitChild(tn, v));
        }
        return this.alternatives(newAlts);
    }

    public Node alternatives(List<TypeNode> alternatives) {
        AmbUnionType aut = (AmbUnionType) this.copy();
        aut.alternatives = alternatives;
        return aut;
    }

}
