package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

public class ElementValuePair_c extends Term_c implements ElementValuePair {

    protected Id name;
    protected Expr value;

    public ElementValuePair_c(Position pos, Id name, Expr value) {
        super(pos);
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return name.id();
    }

    @Override
    public Id id() {
        return name;
    }

    public ElementValuePair name(String name) {
        if (!name.equals(this.name.id())) {
            ElementValuePair_c n = (ElementValuePair_c) copy();
            n.name = this.name.id(name);
            return n;
        }
        return this;
    }

    @Override
    public Expr value() {
        return value;
    }

    public ElementValuePair value(Expr value) {
        if (!value.equals(this.value)) {
            ElementValuePair_c n = (ElementValuePair_c) copy();
            n.value = value;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr value = (Expr) visitChild(this.value, v);
        return value(value);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ts.checkAnnotationValueConstant(value);
        return this;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write(name + "=");
        print(value, w, pp);
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        w.write(name + "=");
        print(value, w, tr);
    }

    public Term entry() {
        return this;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(value, this, EXIT);
        return succs;
    }

    @Override
    public Term firstChild() {
        return this.value;
    }

}
