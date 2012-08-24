package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
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

    @Override
    public String toString() {
        if (vals.isEmpty()) {
            return "{ }";
        }
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        for (Iterator<AnnotationElementValue> iter = vals.iterator(); iter.hasNext();) {
            AnnotationElementValue v = iter.next();
            sb.append(v);
            if (iter.hasNext()) {
                sb.append(", ");
            }

        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public List<AnnotationElementValue> vals() {
        return Collections.unmodifiableList(vals);
    }

}
