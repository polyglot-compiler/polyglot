package polyglot.ext.jl5.ast;

import polyglot.ast.Ambiguous;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class AmbWildCard extends TypeNode_c implements TypeNode, Ambiguous {
    private TypeNode constraint;
    private boolean isExtendsConstraint;

    public AmbWildCard(Position pos) {
        super(pos);
        constraint = null;
        isExtendsConstraint = true;
    }

    public AmbWildCard(Position pos, TypeNode constraint,
            boolean isExtendsConstraint) {
        super(pos);
        this.constraint = constraint;
        this.isExtendsConstraint = isExtendsConstraint;
    }

    @Override
    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        if (this.constraint != null && !this.constraint.isDisambiguated()) {
            return this;
        }
        JL5TypeSystem ts = (JL5TypeSystem) sc.typeSystem();
        Type t;
        if (this.constraint == null) {
            t = ts.wildCardType(this.position());
        }
        else {
            ReferenceType upperBound = null;
            ReferenceType lowerBound = null;
            if (this.isExtendsConstraint) {
                upperBound = (ReferenceType) constraint.type();
            }
            else {
                lowerBound = (ReferenceType) constraint.type();
            }
            t = ts.wildCardType(this.position(), upperBound, lowerBound);
        }
        return sc.nodeFactory().CanonicalTypeNode(this.position, t);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("?");
        if (constraint != null) {
            w.write(" ");
            w.write(this.isExtendsConstraint ? "extends" : "super");
            w.write(" ");
            constraint.prettyPrint(w, tr);
        }
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        if (this.constraint != null) {
            TypeNode c = (TypeNode) visitChild(this.constraint, v);
            return this.constraint(c);
        }
        return this;
    }

    private AmbWildCard constraint(TypeNode constraint) {
        if (this.constraint == constraint) {
            return this;
        }
        AmbWildCard n = (AmbWildCard) this.copy();
        n.constraint = constraint;
        return n;
    }
}
