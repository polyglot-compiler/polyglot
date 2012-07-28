package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.TypeNode;

/**
 * Represents an annotation on a declaration. It is a subclass of Expr instead of Term in order
 * to simplify its use as an element value.
 *
 */
public interface AnnotationElem extends Expr {

    TypeNode typeName();
    AnnotationElem typeName(TypeNode typeName);

    List<ElementValuePair> elements();

    /**
     * An annotation is a marker annotation if it has no elements
     * @return
     */
    boolean isMarkerAnnotation();
//    NormalAnnotationElem elements(List<ElementValuePair> elements);

    /**
     * An annotation is a single-element annotation if it has one element
     * @return
     */
    boolean isSingleElementAnnotation();
}
