package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.types.*;
import polyglot.util.Position;

public class WildCardType_c extends ReferenceType_c implements WildCardType {
	private ReferenceType upperBound;
	private ReferenceType lowerBound;

	public WildCardType_c(TypeSystem ts, Position position,
			ReferenceType upperBound, ReferenceType lowerBound) {
		super(ts, position);
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
	}

	@Override
	public FieldInstance fieldNamed(String name) {
		for (Iterator i = fields().iterator(); i.hasNext();) {
			FieldInstance fi = (FieldInstance) i.next();
			if (fi.name().equals(name)) {
				return fi;
			}
		}
		return null;
	}

	@Override
	public boolean isCanonical() {
		return true;
	}

	@Override
	public ReferenceType upperBound() {
		return this.upperBound;
	}

	@Override
	public WildCardType upperBound(ReferenceType newUpperBound) {
		if (this.upperBound == newUpperBound) {
			return this;
		}
		WildCardType_c n = (WildCardType_c) this.copy();
		n.upperBound = newUpperBound;
		return n;
	}

	@Override
	public WildCardType lowerBound(ReferenceType newLowerBound) {
		if (this.lowerBound == newLowerBound) {
			return this;
		}
		WildCardType_c n = (WildCardType_c) this.copy();
		n.lowerBound = newLowerBound;
		return n;
	}

	@Override
	public ReferenceType lowerBound() {
		return this.lowerBound;
	}

	@Override
	public List methods() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List fields() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public Type superType() {
		if (this.upperBound().isClass()
				&& !this.upperBound().toClass().flags().isInterface()) {
			return this.upperBound();
		}
		return ts.Object();
	}

	@Override
	public List interfaces() {
		if (this.upperBound().isClass()
				&& this.upperBound().toClass().flags().isInterface()) {
			return Collections.singletonList(this.upperBound());
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public String translate(Resolver c) {
		return this.toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('?');
		if (!ts.Object().equals(this.upperBound)) {
			sb.append(" extends ");
			sb.append(this.upperBound);
		} else if (lowerBound != null) {
			sb.append(" super ");
			sb.append(this.lowerBound);
		}
		return sb.toString();
	}

	@Override
	public boolean equalsImpl(TypeObject t) {
		if (t instanceof WildCardType_c) {
			WildCardType_c that = (WildCardType_c) t;
			if (!(this.upperBound == that.upperBound || (this.upperBound != null && typeSystem()
					.equals(this.upperBound, that.upperBound)))) {
				return false;
			}
			if (!(this.lowerBound == that.lowerBound || (this.lowerBound != null && typeSystem()
					.equals(this.lowerBound, that.lowerBound)))) {
				return false;
			}
			return true;
		}
		return super.equalsImpl(t);
	}

	@Override
	public boolean typeEqualsImpl(Type t) {
		if (t instanceof WildCardType_c) {
			WildCardType_c that = (WildCardType_c) t;
			if (!(this.upperBound == that.upperBound || (this.upperBound != null && typeSystem()
					.typeEquals(this.upperBound, that.upperBound)))) {
				return false;
			}
			if (!(this.lowerBound == that.lowerBound || (this.lowerBound != null && typeSystem()
					.typeEquals(this.lowerBound, that.lowerBound)))) {
				return false;
			}
			return true;
		}
		return super.typeEqualsImpl(t);
	}

	@Override
	public boolean isExtendsConstraint() {
		return !isSuperConstraint();
	}

	@Override
	public boolean isSuperConstraint() {
		return lowerBound != null;
	}

	@Override
	public boolean hasLowerBound() {
		return lowerBound != null;
	}

	@Override
	public boolean descendsFromImpl(Type ancestor) {
		if (super.descendsFromImpl(ancestor)) {
			return true;
		}
		if (ts.isSubtype(upperBound(), ancestor)) {
			return true;
		}

		return false;
	}
}
