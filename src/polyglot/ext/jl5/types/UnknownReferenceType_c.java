package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.List;

import polyglot.types.*;

public class UnknownReferenceType_c extends UnknownType_c implements
		UnknownType, ReferenceType {

	public UnknownReferenceType_c(TypeSystem ts) {
		super(ts);
	}

	@Override
	public Type superType() {
		return this;
	}

	@Override
	public List interfaces() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List members() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List fields() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List methods() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public FieldInstance fieldNamed(String name) {
		return null;
	}

	@Override
	public List methodsNamed(String name) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List methods(String name, List argTypes) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public boolean hasMethod(MethodInstance mi) {
		return false;
	}

	@Override
	public boolean hasMethodImpl(MethodInstance mi) {
		return false;
	}
}
