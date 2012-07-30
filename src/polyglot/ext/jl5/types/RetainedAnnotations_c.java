package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import polyglot.types.Type;
import polyglot.types.TypeObject_c;
import polyglot.util.Position;

public class RetainedAnnotations_c extends TypeObject_c implements
        RetainedAnnotations {

    private Map<Type, Map<String, AnnotationElementValue>> annotations;

    public RetainedAnnotations_c(JL5TypeSystem ts, Position pos) {
        super(ts, pos);
        this.annotations = Collections.emptyMap();
    }

    public RetainedAnnotations_c(
            Map<Type, Map<String, AnnotationElementValue>> annotations,
            JL5TypeSystem ts, Position pos) {
        super(ts, pos);
        this.annotations = annotations;
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    @Override
    public Set<Type> annotationTypes() {
        return Collections.unmodifiableSet(annotations.keySet());
    }

    @Override
    public Map<String, AnnotationElementValue> elementValuePairs(
            Type annotationType) {
        if (annotations.containsKey(annotationType)) {
            return Collections.unmodifiableMap(annotations.get(annotationType));
        }
        return null;
    }

    @Override
    public boolean hasAnnotationType(Type annotationType) {
        return annotations.containsKey(annotationType);
    }

    @Override
    public AnnotationElementValue singleElement(Type annotationType) {
        if (annotations.containsKey(annotationType)) {
            return annotations.get(annotationType).get("value");
        }
        return null;
    }
}
