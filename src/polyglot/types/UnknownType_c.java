package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import jltools.types.Package;
import java.io.*;

public class UnknownType_c extends Type_c implements UnknownType
{
    /** Used for deserializing types. */
    protected UnknownType_c() { }
    
    /** Creates a new type in the given a TypeSystem. */
    public UnknownType_c(TypeSystem ts) {
        this(ts, null);
    }

    public UnknownType_c(TypeSystem ts, Position pos) {
        super(ts, pos);
    }

    public String translate(Context c) {
	throw new InternalCompilerError("Cannot translate an unknown type.");
    }

    public String toString() {
	return "<unknown>";
    }
}
