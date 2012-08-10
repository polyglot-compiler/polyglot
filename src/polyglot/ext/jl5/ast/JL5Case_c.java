package polyglot.ext.jl5.ast;

import polyglot.ast.AmbExpr;
import polyglot.ast.Case_c;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Lit;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5Case_c extends Case_c implements JL5Case {

    public JL5Case_c(Position pos, Expr expr) {
        super(pos, expr);
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        //We can't disambiguate unqualified names until the switch expression
        // is typed.
        if (expr instanceof AmbExpr) {
            return this;
        }
        else return null;
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        if (this.expr == null || this.expr instanceof Lit) {
            return null;
        }
        // We will do type checking vis the resolveCaseLabel method
        return this;
    }

    @Override
    public Node resolveCaseLabel(TypeChecker tc, Type switchType)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        JL5NodeFactory nf = (JL5NodeFactory) tc.nodeFactory();

        if (expr == null) {
            return this;
        }
        else if (switchType.isClass() && !isNumericSwitchType(switchType, ts)) {
            // must be an enum...
            if (expr instanceof EnumConstant) {
                // we have already resolved the expression
                EnumConstant ec = (EnumConstant) expr;
                return this.value(ec.enumInstance().ordinal());
            }
            else if (expr instanceof AmbExpr) {
                AmbExpr amb = (AmbExpr) expr;
                EnumInstance ei =
                        ts.findEnumConstant(switchType.toReference(),
                                            amb.name());
                Receiver r =
                        nf.CanonicalTypeNode(Position.compilerGenerated(),
                                             switchType);
                EnumConstant e =
                        nf.EnumConstant(expr.position(), r, amb.id())
                          .enumInstance(ei);
                e = (EnumConstant) e.type(ei.type());
                return this.expr(e).value(ei.ordinal());
            }
            else {
                throw new InternalCompilerError("Unexpected case label " + expr);
            }
        }

        // switch type is not a class.
        JL5Case_c n = this;

        if (expr.isTypeChecked()) {
            // all good, nothing to do.
        }
        else if (expr instanceof AmbExpr) {
            AmbExpr amb = (AmbExpr) expr;
            //Disambiguate and typecheck
            Expr e =
                    (Expr) tc.nodeFactory()
                             .disamb()
                             .disambiguate(amb,
                                           tc,
                                           expr.position(),
                                           null,
                                           amb.id());
            e = (Expr) e.visit(tc);
            n = (JL5Case_c) expr(e);
        }
        else {
            // try type checking it
            n = (JL5Case_c) this.expr((Expr) this.expr.visit(tc));
        }

        if (!n.expr().constantValueSet()) {
            // Not ready yet; pass will get rerun.
            return n;
        }
        if (n.expr().isConstant()) {
            Object o = n.expr().constantValue();
            if (o instanceof Number && !(o instanceof Long)
                    && !(o instanceof Float) && !(o instanceof Double)) {
                return n.value(((Number) o).longValue());
            }
            else if (o instanceof Character) {
                return n.value(((Character) o).charValue());
            }
        }
        throw new SemanticException("Case label must be an integral constant.",
                                    position());
    }

    private boolean isNumericSwitchType(Type switchType, JL5TypeSystem ts) {
        if (ts.Char().equals(switchType)
                || ts.wrapperClassOfPrimitive(ts.Char()).equals(switchType)) {
            return true;
        }
        if (ts.Byte().equals(switchType)
                || ts.wrapperClassOfPrimitive(ts.Byte()).equals(switchType)) {
            return true;
        }
        if (ts.Short().equals(switchType)
                || ts.wrapperClassOfPrimitive(ts.Short()).equals(switchType)) {
            return true;
        }
        if (ts.Int().equals(switchType)
                || ts.wrapperClassOfPrimitive(ts.Int()).equals(switchType)) {
            return true;
        }
        return false;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (expr == null) {
            w.write("default:");
        }
        else {
            w.write("case ");
            JL5TypeSystem ts =
                    expr.type() == null ? null
                            : (JL5TypeSystem) expr.type().typeSystem();
            if (ts != null && expr.type().isReference()
                    && expr.type().isSubtype(ts.toRawType(ts.Enum()))) {
                // this is an enum	            
                Field f = (Field) expr;
                w.write(f.name());
            }
            else {
                print(expr, w, tr);
            }
            w.write(":");
        }
    }

}
