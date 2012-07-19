package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.AnnotationElemInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

public class NormalAnnotationElem_c extends AnnotationElem_c implements NormalAnnotationElem {

    protected List<ElementValuePair> elements;

    public NormalAnnotationElem_c(Position pos, TypeNode typeName, List<ElementValuePair> elements){
        super(pos, typeName);
        this.elements = ListUtil.copy(elements, true);
    }
    
    @Override
    public List<ElementValuePair> elements(){
        return Collections.unmodifiableList(this.elements);
    }
    
    @Override
    public NormalAnnotationElem elements(List<ElementValuePair> elements){
        NormalAnnotationElem_c n = (NormalAnnotationElem_c) copy();
        n.elements = ListUtil.copy(elements, true);
        return n;
    }

    protected Node reconstruct(TypeNode tn, List<ElementValuePair> elements){
        if (tn != this.typeName || !CollectionUtil.equals(elements, this.elements)) {
            NormalAnnotationElem_c n = (NormalAnnotationElem_c) copy();
            n.typeName = tn;
            n.elements = ListUtil.copy(elements, true);
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v){
        TypeNode tn = (TypeNode) visitChild(this.typeName, v);
        List<ElementValuePair> elements = visitList(this.elements, v);
        return reconstruct(tn, elements);
    }
   
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem)tc.typeSystem(); 
        Context c = tc.context();
        // check that elements refer to annotation element instances
        for (ElementValuePair next : elements()) {
            AnnotationElemInstance ai = ts.findAnnotation(typeName().type().toReference(), next.name(), c.currentClass());
            // and value has to be the right type
            if (! ts.isImplicitCastValid(next.value().type(), ai.type()) &&
                ! ts.equals(next.value().type(), ai.type()) &&
                ! ts.numericConversionValid(ai.type(), next.value().constantValue()) && 
                ! ts.isBaseCastValid(next.value().type(), ai.type()) &&
                ! ts.numericConversionBaseValid(ai.type(), next.value().constantValue())){
                throw new SemanticException("The type of the value: "+next.value().type()+" for element: "+next.name()+" does not match the declared annotation type: "+ai.type(), next.value().position());
            }
        }

        // check all elements assigned values or have defaults
        List<AnnotationElemInstance> requiredAnnots = ((JL5ParsedClassType)typeName().type()).annotationElems();
        for (AnnotationElemInstance next : requiredAnnots) {
            if (!next.hasDefault()){
                // if the annotation decl doesn't have a default value
                // then one of the elements must be setting it
                if (!elementForNoDefault(next)){
                    throw new SemanticException("Must have value for element: "+next.name(), typeName().position());
                }
            }
        }
    
        // check duplicat mem val pairs
        ArrayList<ElementValuePair> list = new ArrayList<ElementValuePair>(elements);
        for (int i = 0; i < list.size(); i++){
            ElementValuePair ei = list.get(i);
            for (int j = i+1; j < list.size(); j++){
                ElementValuePair ej = list.get(j);
                if (ei.name().equals(ej.name())){
                    throw new SemanticException("Duplicate annotation member value name in "+this.typeName(), ej.position());
                }
            }
        }            
        
        return super.typeCheck(tc);
    }

    protected boolean elementForNoDefault(AnnotationElemInstance ai){
        for (ElementValuePair e : elements) {
            if (e.name().equals(ai.name())) return true;
        }
        return false;
    }
    
    @Override
    public void translate(CodeWriter w, Translator tr){
        super.translate(w, tr);
        w.write("(");
        for (Iterator<ElementValuePair> it = elements().iterator(); it.hasNext(); ){
            print(it.next(), w, tr);
            if (it.hasNext()){
                w.write(",");
            }
        }
        w.write(") ");
    }
}
