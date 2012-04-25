package polyglot.ext.jl5.types;

import polyglot.types.MethodInstance;
import polyglot.types.Type;

public interface AnnotationElemInstance extends MethodInstance {

    Type type();

    boolean hasDefault();
}
