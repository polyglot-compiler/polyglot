package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import jltools.types.Package;
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

    public abstract String translate(Context c);

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

    public final boolean isThrowable() {
	return ts.isThrowable(this); 
    }
    
    public final boolean isUncheckedException() {
	return ts.isUncheckedException(this); 
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
    
    public final boolean isSame(Type t) {
	return ts.isSame(this, t); 
    }
    
    public final boolean descendsFrom(Type ancestor) { 
	return ts.descendsFrom(this, ancestor); 
    }
    
    public final boolean isAssignableSubtype(Type ancestor) {
	return ts.isAssignableSubtype(this, ancestor); 
    }
    
    public final boolean isCastValid(Type toType) {
	return ts.isCastValid(this, toType); 
    }
    
    public final boolean isImplicitCastValid(Type toType) {
	return ts.isImplicitCastValid(this, toType); 
    }
    
    public final boolean isComparable(Type t) {
	return t.typeSystem() == ts;
    }

    public abstract String toString();
}
