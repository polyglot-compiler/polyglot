package polyglot.ext.jl5.types;

import polyglot.types.MethodInstance;
import polyglot.types.Type;

/**
 * An AnnotationTypeElemInstance represents both the element of an annotation type (i.e., a type declared using "@interface")
 * and the method to access that element.
 *
 */
public interface AnnotationTypeElemInstance extends MethodInstance {

    Type type();

    boolean hasDefault();
}
