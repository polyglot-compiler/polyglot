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

    /** Get the base type of the array. */
    public Type base() {
        return base;
    }

    /** Set the base type of the array. */
    public ArrayType base(Type base) {
	ArrayType_c n = (ArrayType_c) copy();
	n.base = base;
	return n;
    }

    /** Get the ulitimate base type of the array. */
    public Type ultimateBase() {
        if (base.isArray()) {
            return base.toArray().ultimateBase();
        }

        return base;
    }

    public int dims() {
        if (!base.isArray()) {
            return 1;
        } else {
            return 1 + base.toArray().dims();
        }
    }

    public String toString() {
        return base.toString() + "[]";
    }

    /** Translate the type. */
    public String translate(Resolver c) {
	return ts.translateArray(c, this);
    }

    /** Returns true iff the type is canonical. */
    public boolean isCanonical() {
	return base.isCanonical();
    }

    public boolean isArray() { return true; }
    public ArrayType toArray() { return this; }

    /** Get the methods implemented by the array type. */
    public List methods() {
	return Collections.unmodifiableList(methods);
    }

    /** Get the fields of the array type. */
    public List fields() {
	return Collections.unmodifiableList(fields);
    }

    /** Get the clone() method. */
    public MethodInstance cloneMethod() {
	return (MethodInstance) methods.get(0);
    }

    /** Get a field of the type by name. */
    public FieldInstance fieldNamed(String name) {
        FieldInstance fi = lengthField();
        return name.equals(fi.name()) ? fi : null;
    }

    /** Get the length field. */
    public FieldInstance lengthField() {
	return (FieldInstance) fields.get(0);
    }

    /** Get the super type of the array type. */
    public Type superType() {
	return ts.Object();
    }

    /** Get the interfaces implemented by the array type. */
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

    /** Restore the type after deserialization. */
    public TypeObject restore_() throws SemanticException {
	Type base = (Type) this.base.restore();

	if (base != this.base) {
	    return base(base);
	}

	return this;
    }
}
