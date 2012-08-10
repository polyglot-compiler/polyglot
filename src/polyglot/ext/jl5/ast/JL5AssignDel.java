package polyglot.ext.jl5.ast;

import polyglot.ast.Assign;
import polyglot.ast.Node;
import polyglot.ast.Variable;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.TypeChecker;

public class JL5AssignDel extends JL5Del {
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Assign a = (Assign) this.node();
        Type t = a.left().type();
        Type s = a.right().type();

        TypeSystem ts = tc.typeSystem();

        if (!(a.left() instanceof Variable)) {
            throw new SemanticException("Target of assignment must be a variable.",
                                        a.position());
        }

        if (a.operator() == Assign.ASSIGN) {
            if (!ts.isImplicitCastValid(s, t) && !ts.typeEquals(s, t)
                    && !ts.numericConversionValid(t, a.right().constantValue())) {

                throw new SemanticException("Cannot assign " + s + " to " + t
                        + ".", a.position());
            }

            return a.type(t);
        }

        if (a.operator() == Assign.ADD_ASSIGN) {
            // t += s
            if (ts.typeEquals(t, ts.String())
                    && ts.canCoerceToString(s, tc.context())) {
                return a.type(ts.String());
            }

            if (isNumeric(t) && isNumeric(s)) {
                return a.type(ts.promote(numericType(t), numericType(s)));
            }

            throw new SemanticException("The " + a.operator()
                                                + " operator must have "
                                                + "numeric or String operands.",
                                        a.position());
        }

        if (a.operator() == Assign.SUB_ASSIGN
                || a.operator() == Assign.MUL_ASSIGN
                || a.operator() == Assign.DIV_ASSIGN
                || a.operator() == Assign.MOD_ASSIGN) {
            if (isNumeric(t) && isNumeric(s)) {
                return a.type(ts.promote(numericType(t), numericType(s)));
            }

            throw new SemanticException("The " + a.operator()
                                                + " operator must have "
                                                + "numeric operands.",
                                        a.position());
        }

        if (a.operator() == Assign.BIT_AND_ASSIGN
                || a.operator() == Assign.BIT_OR_ASSIGN
                || a.operator() == Assign.BIT_XOR_ASSIGN) {
            if (isBoolean(t) && isBoolean(s)) {
                return a.type(ts.Boolean());
            }

            if (ts.isImplicitCastValid(t, ts.Long())
                    && ts.isImplicitCastValid(s, ts.Long())) {
                return a.type(ts.promote(numericType(t), numericType(s)));
            }

            throw new SemanticException("The "
                                                + a.operator()
                                                + " operator must have "
                                                + "integral or boolean operands.",
                                        a.position());
        }

        if (a.operator() == Assign.SHL_ASSIGN
                || a.operator() == Assign.SHR_ASSIGN
                || a.operator() == Assign.USHR_ASSIGN) {
            if (ts.isImplicitCastValid(t, ts.Long())
                    && ts.isImplicitCastValid(s, ts.Long())) {
                // Only promote the left of a shift.
                return a.type(ts.promote(numericType(t)));
            }

            throw new SemanticException("The " + a.operator()
                                                + " operator must have "
                                                + "integral operands.",
                                        a.position());
        }

        throw new InternalCompilerError("Unrecognized assignment operator "
                + a.operator() + ".");
    }

    private boolean isNumeric(Type t) {
        if (t.isNumeric()) return true;
        JL5TypeSystem ts = (JL5TypeSystem) t.typeSystem();

        if (ts.isPrimitiveWrapper(t)) {
            return ts.primitiveTypeOfWrapper(t).isNumeric();
        }
        return false;
    }

    private boolean isBoolean(Type t) {
        if (t.isBoolean()) return true;
        JL5TypeSystem ts = (JL5TypeSystem) t.typeSystem();

        if (ts.isPrimitiveWrapper(t)) {
            return ts.primitiveTypeOfWrapper(t).isBoolean();
        }
        return false;
    }

    private Type numericType(Type t) {
        if (t.isNumeric()) return t;
        JL5TypeSystem ts = (JL5TypeSystem) t.typeSystem();

        if (ts.isPrimitiveWrapper(t)) {
            return ts.primitiveTypeOfWrapper(t);
        }
        return t;
    }

}
