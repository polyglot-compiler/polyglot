package polyglot.ext.jl5.ast;

import java.util.LinkedList;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.visit.NodeVisitor;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

public class MarkerAnnotationElem_c extends NormalAnnotationElem_c implements MarkerAnnotationElem {

    public MarkerAnnotationElem_c(Position pos, TypeNode typeName){
        super(pos, typeName, new TypedList(new LinkedList(), ElementValuePair.class, false));
    }

    public Node visitChildren(NodeVisitor v){
        return super.visitChildren(v);
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return super.typeCheck(tc);
    }
    
    public void translate(CodeWriter w, Translator tr){
        super.translate(w, tr);
    }
}
