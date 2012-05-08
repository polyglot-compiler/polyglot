package polyglot.ext.jl5.visit;

import java.util.Collections;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.visit.AscriptionVisitor;

/**
 * Translate implicit boxing and unboxing to explicit code.
 */
public class AutoBoxer extends AscriptionVisitor {

	public AutoBoxer(Job job, JL5TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}

	@Override
	public Expr ascribe(Expr e, Type toType) throws SemanticException {
		Type fromType = e.type();
		JL5TypeSystem ts = (JL5TypeSystem) this.ts;
		if (toType.isPrimitive() && !toType.isVoid() && !fromType.isPrimitive()
				&& !fromType.isSubtype(ts.toRawType(ts.Enum()))) {
			// going from a wrapper object to a primitive type
			// translate e to e.XXXvalue() where XXX is int, long, double, etc.
			ClassType wrapperType = ((JL5TypeSystem) ts)
					.wrapperClassOfPrimitive(toType.toPrimitive());
			String methodName = toType.toPrimitive().name() + "Value";
			Call call = nf.Call(e.position(), e, methodName);
			call = (Call) call.type(toType);
			call = call.methodInstance(ts.findMethod(wrapperType, methodName,
					Collections.emptyList(), this.context().currentClass()));
			return call;
		} else if (!toType.isPrimitive() && fromType.isPrimitive()
				&& !fromType.isVoid() && !ts.String().equals(toType)) {
			// going from a primitive value to a wrapper type.
			// translate e to XXX.valueOf(e), where XXX is the
			// java.lang.Integer, java.lang.Double, etc.
			String methodName = "valueOf";
			ClassType wrapperType = ((JL5TypeSystem) ts)
					.wrapperClassOfPrimitive(fromType.toPrimitive());
			TypeNode tn = nf.CanonicalTypeNode(e.position(), wrapperType);
			Call call = nf.Call(e.position(), tn, methodName, e);
			call = (Call) call.type(wrapperType);
			call = call.methodInstance(ts.findMethod(wrapperType, methodName,
					CollectionUtil.list(fromType), this.context()
							.currentClass()));
			return call;

		}
		return super.ascribe(e, toType);
	}

}
