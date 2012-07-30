package polyglot.ext.jl5.types;

import java.util.Map;
import java.util.Set;

import polyglot.types.Type;
import polyglot.types.TypeObject;

/**
 * An RetainedAnnotations object represents the annotations that should be
 * retained in the type information of a type object. This is a subset of the
 * annotations given on the type object's declaration. 
 * See JLS 3rd ed, 9.6.1.2, Retention.
 *
 */
public interface RetainedAnnotations extends TypeObject {
    /**
     * The annotations that have been used.
     */
    Set<Type> annotationTypes();

    /**
     * Get the element value pairs for the given annotation type.
     * Returns null if there is no annotation for the annotation type.
     */
    Map<String, AnnotationElementValue> elementValuePairs(Type annotationType);

    boolean hasAnnotationType(Type annotationType);

    /**
     * If annotationType is a single element annotation type,
     * then return the element (or null if there is no
     * annotation for this annotation type).
     */
    AnnotationElementValue singleElement(Type annotationType);
}
