package polyglot.ext.jl5.ast;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import polyglot.ast.Node;
import polyglot.ext.jl5.types.*;
import polyglot.types.ArrayType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class JL5CanonicalTypeNode_c extends polyglot.ast.CanonicalTypeNode_c {

	public JL5CanonicalTypeNode_c(Position pos, Type type) {
		super(pos, makeRawIfNeeded(type, pos));
	}

	private static Type makeRawIfNeeded(Type type, Position pos) {
		if (type instanceof JL5ParsedClassType
				&& !((JL5ParsedClassType) type).typeVariables().isEmpty()) {
			// needs to be a raw type
			JL5TypeSystem ts = (JL5TypeSystem) type.typeSystem();
			return ts.rawClass((JL5ParsedClassType) type, pos);
		}
		return type;
	}

	public Node typeCheck(TypeChecker tc) throws SemanticException {
		Type t = type();
		if (t instanceof JL5SubstClassType) {
			JL5SubstClassType st = (JL5SubstClassType) type();

			JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
			// first we must perform capture conversion. see beginning of JLS
			// 4.5
			JL5SubstClassType capCT = (JL5SubstClassType) ts
					.applyCaptureConversion(st);

			for (int i = 0; i < capCT.actuals().size(); i++) {
				TypeVariable ai = capCT.base().typeVariables().get(i);
				Type xi = (Type) capCT.actuals().get(i);

				// require that arguments obey their bounds
				if (!ts.isSubtype(xi, capCT.subst().substType(ai.upperBound()))) {
					throw new SemanticException("Type argument "
							+ st.actuals().get(i)
							+ " is not a subtype of its declared bound "
							+ ai.upperBound(), position());
				}
			}
		}
		// check for uses of type variables in static contexts
		if (tc.context().inStaticContext()) {
			for (TypeVariable tv : findInstanceTypeVariables(t)) {
				throw new SemanticException("Type variable " + tv
						+ " cannot be used in a static context", this.position);

			}
		}
		if (tc.context().currentClass() != null) {
			if (tc.context().currentClass().isNested()
					&& !tc.context().currentClass().isInnerClass()) {
				for (TypeVariable tv : findInstanceTypeVariables(t)) {
					if (!tv.declaringClass()
							.equals(tc.context().currentClass())) {
						throw new SemanticException("Type variable " + tv
								+ " of class " + tv.declaringClass()
								+ " cannot be used in a nested class",
								this.position);
					}
				}
			}

		}

		return super.typeCheck(tc);
	}

	private Set<TypeVariable> findInstanceTypeVariables(Type t) {
		Set<TypeVariable> s = new LinkedHashSet<TypeVariable>();
		findInstanceTypeVariables(t, s);
		return s;
	}

	private void findInstanceTypeVariables(Type t, Set<TypeVariable> tvs) {
		if (t instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) t;
			if (tv.declaredIn() == TypeVariable.TVarDecl.CLASS_TYPE_VARIABLE) {
				tvs.add(tv);
			}
		}
		if (t instanceof ArrayType) {
			ArrayType at = (ArrayType) t;
			findInstanceTypeVariables(at.base(), tvs);
		}
		if (t instanceof WildCardType) {
			WildCardType at = (WildCardType) t;
			findInstanceTypeVariables(at.upperBound(), tvs);
		}
		if (t instanceof JL5SubstClassType) {
			JL5SubstClassType ct = (JL5SubstClassType) t;
			for (Type at : (List<Type>) ct.actuals()) {
				findInstanceTypeVariables(at, tvs);
			}
		}
		if (t instanceof IntersectionType) {
			IntersectionType it = (IntersectionType) t;
			for (Type at : it.bounds()) {
				findInstanceTypeVariables(at, tvs);
			}
		}
	}

}
