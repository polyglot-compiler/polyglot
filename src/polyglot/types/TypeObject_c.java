package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import java.io.*;

public abstract class TypeObject_c implements TypeObject
{
    protected transient TypeSystem ts;
    protected Position position;
    protected Resolver resolver;

    /** Used for deserializing types. */
    protected TypeObject_c() { }
    
    /** Creates a new type in the given a TypeSystem. */
    public TypeObject_c(TypeSystem ts) {
	this(ts, null);
    }

    public TypeObject_c(TypeSystem ts, Position pos) {
	this.ts = ts;
	this.position = pos;
    }

    public Object copy() {
        try {
	    return clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalCompilerError("Java clone() wierdness.");
	}
    }

    public Resolver resolver() {
        return resolver;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

    public Position position() {
        return position;
    }

    public TypeObject restore() throws SemanticException {
	return this;
    }

    private void writeObject( ObjectOutputStream out) throws IOException {
	out.writeObject(position);
    }

    private void readObject( ObjectInputStream in) throws IOException,
					       ClassNotFoundException {
	if (in instanceof TypeInputStream) {
	    ts = ((TypeInputStream) in).getTypeSystem();
	}

	position = (Position) in.readObject();
    }
}
