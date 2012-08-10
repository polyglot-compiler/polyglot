package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Unary_c;
import polyglot.ast.Variable;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.TypeChecker;

public class JL5Unary_c extends Unary_c implements JL5Unary {

    public JL5Unary_c(Position pos, Operator op, Expr expr) {
        super(pos, op, expr);
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        if (!ts.isPrimitiveWrapper(expr.type())) {
            return super.typeCheck(tc);
        }

        if (op == POST_INC || op == POST_DEC || op == PRE_INC || op == PRE_DEC) {

            if (!expr.type().isNumeric()) {
                if (!(ts.isPrimitiveWrapper(expr.type()) && (ts.primitiveTypeOfWrapper(expr.type()).isNumeric()))) {
                    throw new SemanticException("Operand of " + op
                            + " operator must be numeric.", expr.position());
                }
            }

            if (!(expr instanceof Variable)) {
                throw new SemanticException("Operand of " + op
                        + " operator must be a variable.", expr.position());
            }

            if (((Variable) expr).flags().isFinal()) {
                throw new SemanticException("Operand of "
                                                    + op
                                                    + " operator must be a non-final variable.",
                                            expr.position());
            }

            return type(expr.type());
        }

        if (op == BIT_NOT) {
            if (!ts.isImplicitCastValid(expr.type(), ts.Long())) {
                throw new SemanticException("Operand of " + op
                        + " operator must be numeric.", expr.position());
            }

            if (ts.isPrimitiveWrapper(expr.type())) {
                return type(ts.promote(ts.primitiveTypeOfWrapper(expr.type())));
            }
            else {
                return type(ts.promote(expr.type()));
            }
        }

        if (op == NEG || op == POS) {
            if (!expr.type().isNumeric()) {
                if (!(ts.isPrimitiveWrapper(expr.type()) && (ts.primitiveTypeOfWrapper(expr.type()).isNumeric()))) {
                    throw new SemanticException("Operand of " + op
                            + " operator must be numeric.", expr.position());
                }
            }

            if (ts.isPrimitiveWrapper(expr.type())) {
                return type(ts.promote(ts.primitiveTypeOfWrapper(expr.type())));
            }
            else {
                return type(ts.promote(expr.type()));
            }
        }

        if (op == NOT) {
            if (!expr.type().isBoolean()) {
                if (!(ts.isPrimitiveWrapper(expr.type()) && (ts.primitiveTypeOfWrapper(expr.type()).isBoolean()))) {
                    throw new SemanticException("Operand of " + op
                            + " operator must be boolean.", expr.position());
                }
            }

            return type(ts.Boolean());
        }

        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        JL5TypeSystem ts = (JL5TypeSystem) av.typeSystem();

        try {
            if (child == expr) {
                Type childType = child.type();
                if (ts.isPrimitiveWrapper(childType))
                    childType = ts.primitiveTypeOfWrapper(childType);
                if (op == POST_INC || op == POST_DEC || op == PRE_INC
                        || op == PRE_DEC) {
                    if (ts.isImplicitCastValid(childType, av.toType())) {
                        return ts.promote(childType);
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == NEG || op == POS) {
                    if (ts.isImplicitCastValid(childType, av.toType())) {
                        return ts.promote(childType);
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == BIT_NOT) {
                    if (ts.isImplicitCastValid(childType, av.toType())) {
                        return ts.promote(childType);
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == NOT) {
                    return ts.Boolean();
                }
            }
        }
        catch (SemanticException e) {
        }

        return child.type();
    }

}
