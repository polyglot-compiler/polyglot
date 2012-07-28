package polyglot.ext.jl5.ast;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Expr_c;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class AnnotationElem_c extends Expr_c implements AnnotationElem {

    protected TypeNode typeName;
    protected List<ElementValuePair> elements;

    public AnnotationElem_c(Position pos, TypeNode typeName,
            List<ElementValuePair> elements) {
        super(pos);
        this.typeName = typeName;
        this.elements = ListUtil.copy(elements, true);
    }
    public AnnotationElem_c(Position pos, TypeNode typeName){
        super(pos);
        this.typeName = typeName;
        this.elements = Collections.emptyList();
    }

    @Override
    public TypeNode typeName(){
        return typeName;
    }

    @Override
    public AnnotationElem typeName(TypeNode typeName){
        if (!typeName.equals(this.typeName)){
            AnnotationElem_c n = (AnnotationElem_c) copy();
            n.typeName = typeName;
            return n;
        }
        return this;
    }

    protected AnnotationElem_c reconstruct(TypeNode typeName){
        if (!typeName.equals(this.typeName)){
            AnnotationElem_c n = (AnnotationElem_c) copy();
            n.typeName = typeName;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v){
        TypeNode tn = (TypeNode)visitChild(this.typeName, v);
        return reconstruct(tn);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // only make annotation elements out of annotation types
        if (!typeName.type().isClass() || !JL5Flags.isAnnotation(typeName.type().toClass().flags())) {
            throw new SemanticException("Annotation: "+typeName+" must be an annotation type, ", position());

        }
        return type(typeName.type());
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("@");
        print(typeName, w, pp);
        if (this.isMarkerAnnotation()) {
            // marker annotation, so no values to print out.
            return;
        }
        w.write("(");

        // Single-element annotation named "value": special case
        if (this.isSingleElementAnnotation()) {
            print(elements().get(0).value(), w, pp);
        }
        else {

            for (Iterator<ElementValuePair> it = elements().iterator(); it
                    .hasNext();) {
                print(it.next(), w, pp);
                if (it.hasNext()) {
                    w.write(", ");
                }
            }
        }
        w.write(") ");
    }

    public Term entry() {
        return this;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public String toString(){
        return "Annotation Type: "+typeName();
    }

    @Override
    public Term firstChild() {
        return typeName;
    }

    @Override
    public List<ElementValuePair> elements() {
        return this.elements;
    }

    @Override
    public boolean isMarkerAnnotation() {
        return elements().isEmpty();
    }

    @Override
    public boolean isSingleElementAnnotation() {
        return elements().size() == 1
                && elements().get(0).name()
                        .equals("value");
    }

}
