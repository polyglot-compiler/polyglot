package carray.ast;

import polyglot.ast.ArrayAccess;
import polyglot.ast.ArrayAccessAssign_c;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import carray.types.CarrayTypeSystem;
import carray.types.ConstArrayType;

/**
 * An <code>Assign</code> represents a Java assignment expression.
 * This class extends Assign_c to implement the restriction that
 * elements of a const array cannot be modified.
 */
public class CarrayAssign_c extends ArrayAccessAssign_c {
    public CarrayAssign_c(Position pos, ArrayAccess left, Operator op,
            Expr right) {
        super(pos, left, op, right);
    }

    /**
     * Type check the expression.
     * The only change is that elements of a const array cannot be modified.
     *
     */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        CarrayTypeSystem ts = (CarrayTypeSystem) tc.typeSystem();

        Type t = left.type();

        // check that the left is an assignable location.
        if (left instanceof ArrayAccess) {
            ArrayAccess a = (ArrayAccess) left;
            if (a.array().type() instanceof ConstArrayType
                    && ((ConstArrayType) a.array().type()).isConst()) {
                throw new SemanticException("Cannot assign a value to an element "
                                                    + "of a const array.",
                                            position());
            }
        }

        // let the super class deal with the rest.
        return super.typeCheck(tc);
    }
}
