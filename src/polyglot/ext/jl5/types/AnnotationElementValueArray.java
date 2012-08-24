package polyglot.ext.jl5.types;

import java.util.List;

/**
 * Represents an element value of an annotation that is a constant. See JLS 3rd ed., 9.7.
 *
 */
public interface AnnotationElementValueArray extends AnnotationElementValue {
    List<AnnotationElementValue> vals();
}
