package polyglot.ext.jl5.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class IntersectionType_c extends ClassType_c implements IntersectionType {

	protected List<ReferenceType> bounds;

	// protected List<Type> concreteBounds;

	protected TypeVariable boundOf_;

	public IntersectionType_c(TypeSystem ts, Position pos,
			List<ReferenceType> bounds) {
		super(ts, pos);
		this.bounds = bounds;
		checkBounds();
	}

	private void checkBounds() {
		if (this.bounds == null || this.bounds.size() < 2) {
			throw new InternalCompilerError(
					"Intersection type needs at least two elements: "
							+ this.bounds);
		}
	}

	public List<ReferenceType> bounds() {
		if (bounds == null || bounds.size() == 0) {
			bounds = new ArrayList<ReferenceType>();
			bounds.add(ts.Object());
		}
		return bounds;
	}

	public String translate(Resolver c) {
		StringBuffer sb = new StringBuffer();// ("intersection[ ");
		for (Iterator<ReferenceType> iter = bounds.iterator(); iter.hasNext();) {
			Type b = iter.next();
			sb.append(b.translate(c));
			if (iter.hasNext())
				sb.append(" & ");
		}
		// sb.append(" ]");
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();// ("intersection[ ");
		sb.append(" ( ");
		for (Iterator<ReferenceType> iter = bounds.iterator(); iter.hasNext();) {
			Type b = iter.next();
			sb.append(b);
			if (iter.hasNext())
				sb.append(" & ");
		}
		// sb.append(" ]");
		sb.append(" ) ");
		return sb.toString();
	}

	// protected List<Type> getConcreteBounds() {
	// if (concreteBounds == null) {
	// concreteBounds = ((JL5TypeSystem)
	// typeSystem()).concreteBounds(this.bounds());
	// }
	// return concreteBounds;
	// }

	public Type superType() {
		if (bounds.isEmpty()) {
			return ts.Object();
		}
		Type t = bounds.get(0);
		if (t.isClass() && !t.toClass().flags().isInterface()) {
			return t;
		}
		return ts.Object();

		// return getSyntheticClass().superType();
	}

	@Override
	public List constructors() {
		return Collections.emptyList();
	}

	// protected ParsedClassType syntheticClass = null;
	//
	// protected ClassType getSyntheticClass() {
	// if (syntheticClass == null) {
	// syntheticClass = typeSystem().createClassType();
	// ArrayList<Type> onlyClasses = new ArrayList<Type>();
	// for (ReferenceType t : getConcreteBounds()) {
	// if (t.isClass() && ((ClassType)t).flags().isInterface())
	// syntheticClass.addInterface(t);
	// else
	// onlyClasses.add(t);
	// }
	// if (onlyClasses.size() > 0) {
	// Collections.sort(onlyClasses, new Comparator<ReferenceType>() {
	// public int compare(ReferenceType o1, ReferenceType o2) {
	// JL5TypeSystem ts = (JL5TypeSystem) typeSystem();
	// if (ts.equals(o1, o2))
	// return 0;
	// if (ts.isSubtype(o1, o2))
	// return -1;
	// return 1;
	// }
	// });
	// syntheticClass.superType(onlyClasses.get(0));
	// }
	// syntheticClass.package_(this.package_());
	// }
	// return syntheticClass;
	// }

	@Override
	public List fields() {
		return Collections.emptyList();
		// return getSyntheticClass().fields();
	}

	@Override
	public Flags flags() {
		return Flags.PUBLIC.set(Flags.FINAL);
		// return getSyntheticClass().flags();
	}

	@Override
	public List interfaces() {
		List interfaces = new ArrayList();
		for (Type t : bounds) {
			if (t.isClass() && t.toClass().flags().isInterface()) {
				interfaces.add(t);
			}
		}
		return interfaces;
		// return getSyntheticClass().interfaces();
	}

	@Override
	public Kind kind() {
		return INTERSECTION;
	}

	@Override
	public List memberClasses() {
		return Collections.emptyList();
	}

	@Override
	public List methods() {
		return Collections.emptyList();
		// return getSyntheticClass().methods();
	}

	@Override
	public String name() {
		return this.toString();
	}

	@Override
	public ClassType outer() {
		return null;
	}

	@Override
	public Package package_() {
		// if (boundOf() != null)
		// return boundOf().package_();
		return null;
	}

	public boolean inStaticContext() {
		return false;
	}

	@Override
	public boolean isImplicitCastValidImpl(Type toType) {
		for (Type b : bounds()) {
			if (typeSystem().isImplicitCastValid(b, toType))
				return true;
		}
		// or just isImplicitCastValid(getSyntaticClass(), toType());
		return false;
	}

	@Override
	public boolean isSubtypeImpl(Type ancestor) {
		for (Type b : bounds()) {
			if (typeSystem().isSubtype(b, ancestor))
				return true;
		}
		return false;
	}

	@Override
	public boolean isCastValidImpl(Type toType) {
		for (Type b : bounds()) {
			if (typeSystem().isCastValid(b, toType))
				return true;
		}
		return false;
	}

	public void boundOf(TypeVariable tv) {
		boundOf_ = tv;
	}

	public TypeVariable boundOf() {
		return boundOf_;
	}

	@Override
	public boolean equalsImpl(TypeObject other) {
		if (!super.equalsImpl(other)) {
			if (other instanceof IntersectionType) {
				IntersectionType it = (IntersectionType) other;
				if (it.bounds().size() != this.bounds().size()) {
					return false;
				}
				for (int i = 0; i < this.bounds().size(); i++) {
					Type ti = this.bounds().get(i);
					Type tj = it.bounds().get(i);
					if (!typeSystem().equals(ti, tj)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean typeEqualsImpl(Type other) {
		if (!super.typeEqualsImpl(other)) {
			if (other instanceof IntersectionType) {
				IntersectionType it = (IntersectionType) other;
				if (it.bounds().size() != this.bounds().size()) {
					return false;
				}
				for (int i = 0; i < this.bounds().size(); i++) {
					Type ti = this.bounds().get(i);
					Type tj = it.bounds().get(i);
					if (!typeSystem().typeEquals(ti, tj)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void setFlags(Flags flags) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setContainer(ReferenceType container) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Job job() {
		throw new UnsupportedOperationException();
		// // TODO Auto-generated method stub
		// return null;
	}

	@Override
	public void setBounds(List<ReferenceType> newBounds) {
		this.bounds = newBounds;
		checkBounds();
	}
}
