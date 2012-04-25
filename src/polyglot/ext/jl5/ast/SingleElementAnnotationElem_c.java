package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.TypeNode;
import polyglot.util.Position;

public class SingleElementAnnotationElem_c extends NormalAnnotationElem_c implements SingleElementAnnotationElem {

    public SingleElementAnnotationElem_c(Position pos, TypeNode typeName, List elements){
        super(pos, typeName, elements);
    }

}
