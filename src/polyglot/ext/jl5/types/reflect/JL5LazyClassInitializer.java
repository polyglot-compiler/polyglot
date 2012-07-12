package polyglot.ext.jl5.types.reflect;

import polyglot.types.LazyClassInitializer;

public interface JL5LazyClassInitializer extends LazyClassInitializer {
    void initAnnotationElems();
}
