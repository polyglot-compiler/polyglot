package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import java.util.*;

/**
 * An <code>ArrayType</code> represents an array of base java types.
 */
public class ArrayType_c extends ReferenceType_c implements ArrayType
{
    protected Type base;
    protected List fields;
    protected List methods;
    protected List interfaces;

    /** Used for deserializing types. */
    protected ArrayType_c() { }

    public ArrayType_c(TypeSystem ts, Position pos, Type base) {
	super(ts, pos);
	this.base = base;

	methods = new ArrayList(1);
	fields = new ArrayList(2);
	interfaces = new ArrayList(2);

	// Add method public Object clone()
	methods.add(ts.methodInstance(position(),
				      this,
	                              Flags.PUBLIC,
				      ts.Object(),
	                              "clone",
				      Collections.EMPTY_LIST,
				      Collections.EMPTY_LIST));

	// Add field public final int length
	fields.add(ts.fieldInstance(position(),
	                            this,
				    Flags.PUBLIC.set(Flags.FINAL),
				    ts.Int(),
				    "length"));

	// Add field public static final Class class
	fields.add(ts.fieldInstance(position(),
	                            this,
				    Flags.PUBLIC.set(Flags.STATIC).set(Flags.FINAL),
				    ts.Class(),
				    "class"));

	interfaces.add(ts.Cloneable());
	interfaces.add(ts.Serializable());
    }

    public Type base() {
        return base;
    }

    public ArrayType base(Type type) {
	ArrayType_c n = (ArrayType_c) copy();
	n.base = base;
	return n;
    }

    public Type ultimateBase() {
        if (base.isArray()) {
	    base.toArray().ultimateBase();
	}

	return base;
    }
    
    public String toString() {
        return base.toString() + "[]";
    }

    public String translate(Context c) {
	return ts.translateArray(c, this);
    }

    public boolean isCanonical() {
	return base.isCanonical();
    }

    public boolean isArray() { return true; }
    public ArrayType toArray() { return this; }

    public List methods() {
	return Collections.unmodifiableList(methods);
    }

    public List fields() {
	return Collections.unmodifiableList(fields);
    }

    public MethodInstance cloneMethod() {
	return (MethodInstance) methods.get(0);
    }

    public FieldInstance fieldNamed(String name) {
        FieldInstance fi = lengthField();
        return name.equals(fi.name()) ? fi : null;
    }

    public FieldInstance lengthField() {
	return (FieldInstance) fields.get(0);
    }

    public Type superType() {
	return ts.Object();
    }

    public List interfaces() {
	return Collections.unmodifiableList(interfaces);
    }

    public int hashCode() {
	return base.hashCode() << 1;
    }

    public boolean equals(Object o) {
        if (o instanceof ArrayType) {
	    ArrayType t = (ArrayType) o;
	    return base.isSame(t.base());
	}

	return false;
    }

    public TypeObject restore() throws SemanticException {
	Type base = (Type) this.base.restore();

	if (base != this.base) {
	    return base(base);
	}

	return this;
    }
}
