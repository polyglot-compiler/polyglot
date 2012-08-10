package polyglot.ext.jl5.types;

import polyglot.types.ReferenceType;

public interface WildCardType extends ReferenceType {

    ReferenceType upperBound();

    boolean hasLowerBound();

    ReferenceType lowerBound();

    boolean isExtendsConstraint();

    boolean isSuperConstraint();

    WildCardType upperBound(ReferenceType newUpperBound);

    WildCardType lowerBound(ReferenceType newLowerBound);

}
