package polyglot.ext.jl5.types;

import java.util.List;

import polyglot.types.TypeObject_c;
import polyglot.util.Position;

public class AnnotationElementValueArray_c extends TypeObject_c implements
        AnnotationElementValueArray {

    private List<AnnotationElementValue> vals;

    public AnnotationElementValueArray_c(JL5TypeSystem ts, Position pos,
            List<AnnotationElementValue> vals) {
        super(ts, pos);
        this.vals = vals;
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

}
