package polyglot.ext.coffer.ast;

import polyglot.ast.*;
import polyglot.ext.coffer.types.*;

public interface ThrowConstraintNode extends Node {
    TypeNode type();
    KeySetNode keys();
    ThrowConstraint constraint();

    ThrowConstraintNode type(TypeNode type);
    ThrowConstraintNode keys(KeySetNode keys);
    ThrowConstraintNode constraint(ThrowConstraint constraint);
}
