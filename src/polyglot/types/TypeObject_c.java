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
    protected TypeExt ext;

    /** Used for deserializing types. */
    protected TypeObject_c() {
    }
    
    /** Creates a new type in the given a TypeSystem. */
    public TypeObject_c(TypeSystem ts) {
        this(ts, null, null);
    }

    public TypeObject_c(TypeSystem ts, TypeExt ext) {
        this(ts, null, ext);
    }

    public TypeObject_c(TypeSystem ts, Position pos) {
        this(ts, pos, null);
    }

    public TypeObject_c(TypeSystem ts, Position pos, TypeExt ext) {
	this.ts = ts;
	this.position = pos;
        if (ext != null) {
            this.setExt(ext);
        }
    }

    public TypeExt ext() {
        return this.ext;
    }

    public TypeObject ext(TypeExt ext) {
        if (this.ext == ext) {
            return this;
        }

        try {
            // use clone here, not copy to avoid copying the ext as well
            TypeObject_c n = (TypeObject_c) super.clone();
            n.setExt(ext);
            return n;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    public void setExt(TypeExt ext) {
        this.ext = ext;
        if (this.ext != null) {
            this.ext.init(this);
        }
    }

    public Object copy() {
        if (this.ext != null) {
            return ext((TypeExt) this.ext.copy());
        }
        else {
            try {
                return (TypeObject_c) super.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new InternalCompilerError("Java clone() weirdness.");
            }
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

        if (o.ext() != null) {
            TypeExt ext = o.ext().restore();
            o.setExt(ext);
        }

	restoring = false;

	return o;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.writeObject(position);
        out.writeObject(ext);
    }

    private void readObject(ObjectInputStream in) throws IOException,
					       ClassNotFoundException {
	if (in instanceof TypeInputStream) {
	    ts = ((TypeInputStream) in).getTypeSystem();
	}

	position = (Position) in.readObject();
        ext = (TypeExt) in.readObject();
    }
}
