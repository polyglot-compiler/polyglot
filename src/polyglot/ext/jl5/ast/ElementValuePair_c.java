package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Expr_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class ElementValuePair_c extends Expr_c implements ElementValuePair {
   
    protected Id name;
    protected Expr value;
    
    public ElementValuePair_c(Position pos, Id name, Expr value){
        super(pos);
        this.name = name;
        this.value = value;
    }

    public String name(){
        return name.id();
    }

    public Id id(){
        return name;
    }

    public ElementValuePair name(String name){
        if (!name.equals(this.name)){
            ElementValuePair_c n = (ElementValuePair_c)copy();
            n.name = this.name.id(name);
            return n;
        }
        return this;
    }

    public Expr value(){
        return value;
    }

    public ElementValuePair value(Expr value){
        if (!value.equals(this.value)){
            ElementValuePair_c n = (ElementValuePair_c)copy();
            n.value = value;
            return n;
        }
        return this;
    }

    protected Node reconstruct(Expr value){
        if (value != this.value){
            ElementValuePair_c n = (ElementValuePair_c) copy();
            n.value = value;
            return n;
        }
        return this;
    }
    
    public Node visitChildren(NodeVisitor v){
        Expr value = (Expr) visitChild(this.value, v);
        return reconstruct(value);
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException{
        JL5TypeSystem ts = (JL5TypeSystem)tc.typeSystem();
        ts.checkAnnotationValueConstant(value);
        return type(value.type());
    }
    
    public void translate(CodeWriter w, Translator tr){
        w.write(name+"=");
        print(value, w, tr);
    }
    
    public Term entry() {
        return this;
    }
    
    public <T> List<T> acceptCFG(CFGBuilder v, List<T> succs) {
        v.visitCFG(value, this, EXIT);
        return succs;
    }

    @Override
    public Term firstChild() {
        return this.value;
    }
        
}
