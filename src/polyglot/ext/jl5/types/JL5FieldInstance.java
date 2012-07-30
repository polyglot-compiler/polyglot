package polyglot.ext.jl5.types;

import polyglot.types.FieldInstance;

public interface JL5FieldInstance extends FieldInstance {

    /**
     * Annotations on the declaration of this type such that the annotation type has
     * a retention policy of annotation.RetentionPolicy.CLASS or annotation.RetentionPolicy.RUNTIME.
     */
    RetainedAnnotations retainedAnnotations();

    void setRetainedAnnotations(RetainedAnnotations createRetainedAnnotations);

}
