package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;
import java.io.*;

/**
 * Abstract implementation of a type object.  Contains a reference to the
 * type system and to the object's position in the code.
 */  
public abstract class TypeObject_c implements TypeObject
{
    protected transient TypeSystem ts;
    protected Position position;

    /** Used for deserializing types. */
    protected TypeObject_c() {
    }
    
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
            return (TypeObject_c) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    public TypeSystem typeSystem() {
        return ts;
    }

    public Position position() {
        return position;
    }

    transient boolean restoring = false;

    public TypeObject restore_() throws SemanticException {
	return this;
    }

    public TypeObject restore() throws SemanticException {
	if (restoring) return this;

	restoring = true;

	TypeObject o = restore_();

	restoring = false;

	return o;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.writeObject(position);
    }

    private void readObject(ObjectInputStream in) throws IOException,
					       ClassNotFoundException {
	if (in instanceof TypeInputStream) {
	    ts = ((TypeInputStream) in).getTypeSystem();
	}

	position = (Position) in.readObject();
    }
}
