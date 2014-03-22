package carray.ast;

import polyglot.ast.ArrayAccess;
import polyglot.ast.ArrayAccessAssign;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;
import carray.types.ConstArrayType;

/**
 * An <code>Assign</code> represents a Java assignment expression.
 * This class extends Assign_c to implement the restriction that
 * elements of a const array cannot be modified.
 */
public class CarrayAssignExt extends CarrayExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * Type check the expression.
     * The only change is that elements of a const array cannot be modified.
     *
     */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ArrayAccessAssign n = (ArrayAccessAssign) this.node();
        Expr left = n.left();
        // check that the left is an assignable location.
        if (left instanceof ArrayAccess) {
            ArrayAccess a = (ArrayAccess) left;
            if (a.array().type() instanceof ConstArrayType
                    && ((ConstArrayType) a.array().type()).isConst()) {
                throw new SemanticException("Cannot assign a value to an element "
                                                    + "of a const array.",
                                            n.position());
            }
        }

        // let the super class deal with the rest.
        return superLang().typeCheck(n, tc);
    }
}
