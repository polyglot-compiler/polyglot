package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.*;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;

public class JL5New_c extends New_c implements JL5New {

	private List<TypeNode> typeArgs;

	public JL5New_c(Position pos, Expr outer, List<TypeNode> typeArgs,
			TypeNode objectType, List argTypes, ClassBody body) {
		super(pos, outer, objectType, argTypes, body);
		this.typeArgs = typeArgs;
	}

	@Override
	public List<TypeNode> typeArgs() {
		return this.typeArgs;
	}

	@Override
	public JL5New typeArgs(List<TypeNode> typeArgs) {
		if (this.typeArgs == typeArgs) {
			return this;
		}
		JL5New_c n = (JL5New_c) this.copy();
		n.typeArgs = typeArgs;
		return n;
	}

	@Override
	public Node visitChildren(NodeVisitor v) {
		JL5New_c n = (JL5New_c) super.visitChildren(v);
		List targs = visitList(n.typeArgs, v);
		return n.typeArgs(targs);
	}

	@Override
	public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
			throws SemanticException {
		JL5New n = (JL5New) super.disambiguateOverride(parent, ar);
		// now do the type args
		n = (JL5New) n.typeArgs(n.visitList(n.typeArgs(), ar));
		return n;
	}

	@Override
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

		List argTypes = new ArrayList(arguments.size());

		for (Iterator i = this.arguments.iterator(); i.hasNext();) {
			Expr e = (Expr) i.next();
			argTypes.add(e.type());
		}

		List<Type> actualTypeArgs = new ArrayList(typeArgs.size());
		for (Iterator i = this.typeArgs.iterator(); i.hasNext();) {
			TypeNode tn = (TypeNode) i.next();
			actualTypeArgs.add(tn.type());
		}

		typeCheckFlags(tc);
		typeCheckNested(tc);

		if (this.body != null) {
			ts.checkClassConformance(anonType);
		}

		ClassType ct = tn.type().toClass();

		if (!ct.flags().isInterface()) {
			Context c = tc.context();
			if (anonType != null) {
				c = c.pushClass(anonType, anonType);
			}
			ci = ts.findConstructor(ct, argTypes, actualTypeArgs,
					c.currentClass());
		} else {
			ci = ts.defaultConstructor(this.position(), ct);
		}

		New n = this.constructorInstance(ci);

		if (anonType != null) {
			// The type of the new expression is the anonymous type, not the
			// base type.
			ct = anonType;
		}

		return n.type(ct);
	}

}
