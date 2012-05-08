package polyglot.ext.jl5.ast;

import polyglot.ast.*;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
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
		// We can't disambiguate unqualified names until the switch expression
		// is typed.
		if (expr instanceof AmbExpr) {
			return this;
		} else
			return null;
	}

	@Override
	public Node typeCheckOverride(Node parent, TypeChecker tc)
			throws SemanticException {
		// We can't typecheck unqualified names until the switch expression
		// is typed.
		if (expr instanceof AmbExpr) {
			return this;
		} else
			return null;
	}

	@Override
	public Node resolveCaseLabel(TypeChecker tc, Type switchType)
			throws SemanticException {
		JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
		JL5NodeFactory nf = (JL5NodeFactory) tc.nodeFactory();

		if (expr == null) {
			return this;
		} else if (switchType.isClass() && !expr.isTypeChecked()) {
			boolean tced = expr.isTypeChecked();
			boolean daed = expr.isDisambiguated();
			AmbExpr amb = (AmbExpr) expr;
			EnumInstance ei = ts.findEnumConstant(switchType.toReference(),
					amb.name());
			Receiver r = nf.CanonicalTypeNode(Position.compilerGenerated(),
					switchType);
			EnumConstant e = nf.EnumConstant(expr.position(), r, amb.id())
					.enumInstance(ei);
			e = (EnumConstant) e.type(ei.type());
			JL5Case_c n = (JL5Case_c) expr(e);
			return n.value(ei.ordinal());
		}

		JL5Case_c n = null;
		if (expr.isTypeChecked()) {
			n = this;
		} else if (!switchType.isClass()) {
			AmbExpr amb = (AmbExpr) expr;
			// Disambiguate and typecheck
			Expr e = (Expr) tc.nodeFactory().disamb()
					.disambiguate(amb, tc, expr.position(), null, amb.id());
			e = (Expr) e.visit(tc);
			n = (JL5Case_c) expr(e);
		} else {
			throw new SemanticException(
					"Unexpected switch type: " + switchType, position());
		}
		//
		// if (!((n.expr().type().isClass())
		// || ts.isImplicitCastValid(n.expr().type(), ts.Int()))) {
		// throw new SemanticException(
		// "Case label must be an enum, byte, char, short, or int.",
		// position());
		// }

		Object o = n.expr().constantValue();
		if (o instanceof Number && !(o instanceof Long)
				&& !(o instanceof Float) && !(o instanceof Double)) {
			return n.value(((Number) o).longValue());
		} else if (o instanceof Character) {
			return n.value(((Character) o).charValue());
		}
		throw new SemanticException("Case label must be an integral constant.",
				position());
	}

	@Override
	public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		if (expr == null) {
			w.write("default:");
		} else {
			w.write("case ");
			JL5TypeSystem ts = expr.type() == null ? null
					: (JL5TypeSystem) expr.type().typeSystem();
			if (ts != null && expr.type().isReference()
					&& expr.type().isSubtype(ts.toRawType(ts.Enum()))) {
				// this is an enum
				Field f = (Field) expr;
				w.write(f.name());
			} else {
				print(expr, w, tr);
			}
			w.write(":");
		}
	}

}
