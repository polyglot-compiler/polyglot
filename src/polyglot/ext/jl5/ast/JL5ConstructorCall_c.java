package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.ConstructorCall_c;
import polyglot.ast.Expr;
import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.types.*;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5ConstructorCall_c extends ConstructorCall_c {
	/**
	 * Is this constructor call a super call to java.lang.Enum?
	 */
	private boolean isEnumConstructorCall;

	public JL5ConstructorCall_c(Position pos, Kind kind, Expr qualifier,
			List arguments, boolean isEnumConstructorCall) {
		super(pos, kind, qualifier, arguments);
		this.isEnumConstructorCall = isEnumConstructorCall;
	}

	@Override
	public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
		ClassType ct = ar.context().currentClass();
		if (ct != null && JL5Flags.isEnum(ct.flags())) {
			if (this.arguments().isEmpty()) {
				// this is an enum decl, so we need to replace a call to the
				// default
				// constructor with a call to java.lang.Enum.Enum(String, int)
				List args = new ArrayList(2);// XXX the right thing to do is
												// change the type of
												// java.lang.Enum instead of
												// adding these dummy params
				args.add(ar.nodeFactory().NullLit(Position.compilerGenerated()));
				args.add(ar.nodeFactory().IntLit(Position.compilerGenerated(),
						IntLit.INT, 0));
				JL5ConstructorCall_c n = (JL5ConstructorCall_c) this
						.arguments(args);
				n.isEnumConstructorCall = true;
				return n.disambiguate(ar);
			}
		}
		return super.disambiguate(ar);
	}

	@Override
	public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		// are we a super call within an enum const decl?
		if (isEnumConstructorCall) {
			boolean translateEnums = ((JL5Options) this.ci.typeSystem()
					.extensionInfo().getOptions()).enumImplClass == null;
			boolean removeJava5isms = ((JL5Options) this.ci.typeSystem()
					.extensionInfo().getOptions()).removeJava5isms;
			if (!removeJava5isms && translateEnums) {
				// we don't print an explicit call to super
				return;
			}
		}
		super.prettyPrint(w, tr);
	}

	@Override
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		ConstructorCall_c n = this;

		TypeSystem ts = tc.typeSystem();
		Context c = tc.context();

		ClassType ct = c.currentClass();
		Type superType = ct.superType();

		Expr qualifier = n.qualifier();
		Kind kind = n.kind();

		// The qualifier specifies the enclosing instance of this inner class.
		// The type of the qualifier must be the outer class of this
		// inner class or one of its super types.
		//
		// Example:
		//
		// class Outer {
		// class Inner { }
		// }
		//
		// class ChildOfInner extends Outer.Inner {
		// ChildOfInner() { (new Outer()).super(); }
		// }
		if (qualifier != null) {
			if (!qualifier.isDisambiguated()) {
				return n;
			}

			if (kind != SUPER) {
				throw new SemanticException("Can only qualify a \"super\""
						+ "constructor invocation.", position());
			}

			if (!superType.isClass() || !superType.toClass().isInnerClass()
					|| superType.toClass().inStaticContext()) {
				throw new SemanticException(
						"The class \""
								+ superType
								+ "\""
								+ " is not an inner class, or was declared in a static "
								+ "context; a qualified constructor invocation cannot "
								+ "be used.", position());
			}

			Type qt = qualifier.type();

			if (!qt.isClass() || !qt.isSubtype(superType.toClass().outer())) {
				throw new SemanticException("The type of the qualifier " + "\""
						+ qt + "\" does not match the immediately enclosing "
						+ "class  of the super class \""
						+ superType.toClass().outer() + "\".",
						qualifier.position());
			}
		}

		if (kind == SUPER) {
			if (!superType.isClass()) {
				throw new SemanticException("Super type of " + ct
						+ " is not a class.", position());
			}

			Expr q = qualifier;

			// If the super class is an inner class (i.e., has an enclosing
			// instance of its container class), then either a qualifier
			// must be provided, or ct must have an enclosing instance of the
			// super class's container class, or a subclass thereof.
			if (q == null && superType.isClass()
					&& superType.toClass().isInnerClass()) {
				ClassType superContainer = superType.toClass().outer();
				// ct needs an enclosing instance of superContainer,
				// or a subclass of superContainer.
				ClassType e = ct;

				while (e != null) {
					// use isImplicitCastValid instead of isSubtype in order to
					// allow unchecked conversion.
					if (e.isImplicitCastValid(superContainer)
							&& ct.hasEnclosingInstance(e)) {
						NodeFactory nf = tc.nodeFactory();
						q = nf.This(position(),
								nf.CanonicalTypeNode(position(), e)).type(e);
						break;
					}
					e = e.outer();
				}

				if (e == null) {
					throw new SemanticException(ct
							+ " must have an enclosing instance"
							+ " that is a subtype of " + superContainer,
							position());
				}
				if (e == ct) {
					throw new SemanticException(
							ct
									+ " is a subtype of "
									+ superContainer
									+ "; an enclosing instance that is a subtype of "
									+ superContainer
									+ " must be specified in the super constructor call.",
							position());
				}
			}

			if (qualifier != q)
				n = (ConstructorCall_c) n.qualifier(q);
		}

		List argTypes = new LinkedList();

		for (Iterator iter = n.arguments().iterator(); iter.hasNext();) {
			Expr e = (Expr) iter.next();
			if (!e.isDisambiguated()) {
				return this;
			}
			argTypes.add(e.type());
		}

		if (kind == SUPER) {
			ct = ct.superType().toClass();
		}

		ConstructorInstance ci = ts.findConstructor(ct, argTypes,
				c.currentClass());
		return n.constructorInstance(ci);
	}

}
