package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.types.Package;
import java.io.*;

/**
 * Abstract implementation of a <code>Type</code>.  This implements most of
 * the "isa" and "cast" methods of the type and methods which just dispatch to
 * the type system.
 */
public abstract class Type_c extends TypeObject_c implements Type
{
    /** Used for deserializing types. */
    protected Type_c() { }
    
    /** Creates a new type in the given a TypeSystem. */
    public Type_c(TypeSystem ts) {
        this(ts, null);
    }

    public Type_c(TypeSystem ts, Position pos) {
        super(ts, pos);
    }

    public abstract String translate(Resolver c);

    public boolean isType() { return true; }
    public boolean isPackage() { return false; }
    public Type toType() { return this; }
    public Package toPackage() { return null; }

    /* To be filled in by subtypes. */
    public boolean isCanonical() { return false; }

    public boolean isPrimitive() { return false; }
    public boolean isNumeric() { return false; }
    public boolean isIntOrLess() { return false; }
    public boolean isLongOrLess() { return false; }
    public boolean isVoid() { return false; }
    public boolean isBoolean() { return false; }
    public boolean isChar() { return false; }
    public boolean isByte() { return false; }
    public boolean isShort() { return false; }
    public boolean isInt() { return false; }
    public boolean isLong() { return false; }
    public boolean isFloat() { return false; }
    public boolean isDouble() { return false; }

    public boolean isReference() { return false; }
    public boolean isNull() { return false; }
    public boolean isClass() { return false; }
    public boolean isArray() { return false; }

    public boolean isThrowable() {
	return false;
    }

    public boolean isUncheckedException() {
	return false;
    }
    
    // Required for 
    /** Returns a non-null iff isClass() returns true. */
    public ClassType toClass() {
	return null;
    }

    /** Returns a non-null iff isNull() returns true. */
    public NullType toNull() {
	return null;
    }

    /** Returns a non-null iff isReference() returns true. */
    public ReferenceType toReference() {
	return null;
    }

    /** Returns a non-null iff isPrimitive() returns true. */
    public PrimitiveType toPrimitive() {
	return null;
    }

    /** Returns a non-null iff isArray() returns true. */
    public ArrayType toArray() {
	return null;
    }

    public ArrayType arrayOf(int dims) {
	return ts.arrayOf(this, dims); 
    }  

    public ArrayType arrayOf() {
	return ts.arrayOf(this);
    }  
    
    public final boolean isSubtype(Type t) {
	return ts.isSubtype(this, t);
    }

    public boolean isSubtypeImpl(Type t) {
	return ts.isSame(this, t) || ts.descendsFrom(this, t);
    }
    
    public final boolean descendsFrom(Type t) {
        return ts.descendsFrom(this, t);
    }

    public boolean descendsFromImpl(Type t) {
        return false;
    }

    public final boolean isSame(Type t) {
        return ts.isSame(this, t);
    }
    
    public boolean isSameImpl(Type t) {
        return t == this;
    }

    public boolean equals(Object o) {
        return o instanceof Type && ts.isSame(this, (Type) o);
    }
    
    public final boolean isCastValid(Type toType) {
	return ts.isCastValid(this, toType);
    }
    
    public boolean isCastValidImpl(Type toType) {
	return false;
    }
    
    public final boolean isImplicitCastValid(Type toType) {
        return ts.isImplicitCastValid(this, toType);
    }

    public boolean isImplicitCastValidImpl(Type toType) {
        return false;
    }

    public final boolean numericConversionValid(long value) {
        return ts.numericConversionValid(this, value);
    }
    
    public boolean numericConversionValidImpl(long value) {
        return false;
    }
    
    public boolean isComparable(Type t) {
	return t.typeSystem() == ts;
    }

    public abstract String toString();
}
