package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Node;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.SemanticException;

/**
 * Marker interface for nodes that can check an annotation.
 *
 */
public interface AnnotatedElement {
    List<AnnotationElem> annotationElems();

    Node annotationCheck(AnnotationChecker ac) throws SemanticException;
}
