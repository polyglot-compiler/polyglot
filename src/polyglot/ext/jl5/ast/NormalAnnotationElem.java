package polyglot.ext.jl5.ast;

import java.util.List;

public interface NormalAnnotationElem extends AnnotationElem {

    List<ElementValuePair> elements();
    NormalAnnotationElem elements(List<ElementValuePair> elements);
}
