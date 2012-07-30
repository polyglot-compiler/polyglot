package polyglot.ext.jl5.types;

import java.util.Map;

import polyglot.types.Type;
import polyglot.types.TypeObject_c;
import polyglot.util.Position;

public class AnnotationElementValueAnnotation_c extends TypeObject_c implements
        AnnotationElementValueAnnotation {
    private Type annotationType;
    private Map<java.lang.String, AnnotationElementValue> annotationElementValues;

    public AnnotationElementValueAnnotation_c(
            JL5TypeSystem ts,
            Position pos,
            Type annotationType,
            Map<java.lang.String, AnnotationElementValue> annotationElementValues) {
        super(ts, pos);
        this.annotationType = annotationType;
        this.annotationElementValues = annotationElementValues;
    }

    @Override
    public boolean isCanonical() {
        return annotationType.isCanonical();
    }

}
