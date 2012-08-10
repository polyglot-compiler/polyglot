package polyglot.ext.jl5.ast;

import polyglot.ast.Conditional_c;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class JL5Conditional_c extends Conditional_c {

    public JL5Conditional_c(Position pos, Expr cond, Expr consequent,
            Expr alternative) {
        super(pos, cond, consequent, alternative);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        Expr e1 = consequent;
        Expr e2 = alternative;
        Type t1 = e1.type();
        Type t2 = e2.type();

        if (!ts.isImplicitCastValid(cond.type(), ts.Boolean())) {
            throw new SemanticException("Condition of ternary expression must be of type boolean.",
                                        cond.position());
        }

        // From the JLS, 3rd ed. section 15.25:
        // If the second and third operands have the same type (which may be
        // the null type), then that is the type of the conditional expression.
        if (ts.typeEquals(t1, t2)) {
            return type(t1);
        }

        // If one of the second and third operands is of type boolean and the type of 
        // the other is of type Boolean, then the type of the conditional expression is bool- ean.
        if ((ts.typeEquals(t1, ts.Boolean()) && ts.typeEquals(t2,
                                                              ts.typeForName("java.lang.Boolean")))
                || (ts.typeEquals(t2, ts.Boolean()) && ts.typeEquals(t1,
                                                                     ts.typeForName("java.lang.Boolean")))) {
            return type(ts.Boolean());
        }

        // If one of the second and third operands is of the null type and the type of 
        // the other is a reference type, then the type of the conditional expression is 
        // that reference type.
        if (t1.isNull()) {
            if (t2.isReference())
                return type(t2);
            else if (t2.isPrimitive())
            // shortcut for lub(null, box(t2))
                return type(ts.boxingConversion(t2));
        }
        if (t2.isNull()) {
            if (t1.isReference())
                return type(t1);
            else if (t1.isPrimitive())
            // shortcut for lub(box(t1), null)
                return type(ts.boxingConversion(t1));
        }

        // Otherwise, if the second and third operands have numeric type, then
        // there are several cases:
        if (t1.isNumeric() && t2.isNumeric()) {
            // If one of the operands is of type byte or Byte and the other is of type 
            // short or Short, then the type of the conditional expression is short.
            if ((t1.isByte() || t1.equals(ts.typeForName("java.lang.Byte")))
                    && (t2.isShort() || t2.equals(ts.typeForName("java.lang.Short")))) {
                return type(ts.Short());
            }
            if ((t2.isByte() || t2.equals(ts.typeForName("java.lang.Byte")))
                    && (t1.isShort() || t1.equals(ts.typeForName("java.lang.Short")))) {
                return type(ts.Short());
            }

            // If one of the operands is of type T where T is byte, short, or char, 
            // and the other operand is a constant expression of type int whose value is 
            // represent- able in type T, then the type of the conditional expression is T.
            if (t1.isIntOrLess() && t2.isInt()
                    && ts.numericConversionValid(t1, e2.constantValue())) {
                return type(t1);
            }

            if (t2.isIntOrLess() && t1.isInt()
                    && ts.numericConversionValid(t2, e1.constantValue())) {
                return type(t2);
            }

            // If one of the operands is of type Byte and the other operand is a constant 
            // expression of type int whose value is representable in type byte, then the type 
            // of the conditional expression is byte.
            if (t1.equals(ts.typeForName("java.lang.Byte")) && t2.isInt()
                    && ts.numericConversionValid(ts.Byte(), e2.constantValue())) {
                return type(ts.Byte());
            }
            if (t2.equals(ts.typeForName("java.lang.Byte")) && t1.isInt()
                    && ts.numericConversionValid(ts.Byte(), e1.constantValue())) {
                return type(ts.Byte());
            }

            // If one of the operands is of type Short and the other operand is a constant expression of 
            // type int whose value is representable in type short, then the type of the 
            // conditional expression is short.
            if (t1.equals(ts.typeForName("java.lang.Short"))
                    && t2.isInt()
                    && ts.numericConversionValid(ts.Short(), e2.constantValue())) {
                return type(ts.Short());
            }
            if (t2.equals(ts.typeForName("java.lang.Short"))
                    && t1.isInt()
                    && ts.numericConversionValid(ts.Short(), e1.constantValue())) {
                return type(ts.Short());
            }

            // If one of the operands is of type Character and the other operand is a 
            // constant expression of type int whose value is representable in type char, 
            // then the type of the conditional expression is char.
            if (t1.equals(ts.typeForName("java.lang.Character")) && t2.isInt()
                    && ts.numericConversionValid(ts.Char(), e2.constantValue())) {
                return type(ts.Char());
            }
            if (t2.equals(ts.typeForName("java.lang.Character")) && t1.isInt()
                    && ts.numericConversionValid(ts.Char(), e1.constantValue())) {
                return type(ts.Char());
            }

            //  Otherwise, binary numeric promotion (5.6.2) is applied to the operand types, 
            // and the type of the conditional expression is the promoted type of the second and
            // third operands. Note that binary numeric promotion performs unboxing 
            // conversion (�5.1.8) and value set conversion (�5.1.13).
            return type(ts.promote(t1, t2));
        }

        //  Otherwise, the second and third operands are of types t1 and t2
        // respectively. Let s1 be the type that results from applying boxing conversion to t1, 
        // and let s2 be the type that results from applying boxing conversion to t2. 
        // The type of the conditional expression is the result of applying capture conversion 
        // (�5.1.10) to lub(s1, s2) (�15.12.2.7).
        ReferenceType s1 = (ReferenceType) ts.boxingConversion(t1);
        ReferenceType s2 = (ReferenceType) ts.boxingConversion(t2);

        LubType lub = ts.lub(this.position, CollectionUtil.list(s1, s2));
        return type(ts.applyCaptureConversion(lub.calculateLub()));
    }

}
