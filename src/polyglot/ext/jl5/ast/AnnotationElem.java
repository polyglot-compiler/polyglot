package polyglot.ext.jl5.ast;

import java.util.List;
import java.util.Map;

import polyglot.ast.Expr;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.AnnotationElementValue;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;

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
     * Convert this AST representation into a suitable type annotation.
     * @throws SemanticException 
     */
    Map<String, AnnotationElementValue> toAnnotationElementValues(
            JL5TypeSystem ts) throws SemanticException;

    /**
     * An annotation is a marker annotation if it has no elements
     * @return
     */
    boolean isMarkerAnnotation();

    /**
     * An annotation is a single-element annotation if it has one element named "value"
     * @return
     */
    boolean isSingleElementAnnotation();
}
