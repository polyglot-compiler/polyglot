package jltools.ext.jl.types;

import jltools.types.*;
import jltools.types.Package;
import jltools.util.Position;

/**
 * An unknown type qualifier.  This is used as a place-holder until types
 * are disambiguated.
 */
public class UnknownQualifier_c extends TypeObject_c implements UnknownQualifier
{
    public UnknownQualifier_c(TypeSystem ts) {
        this(ts, null);
    }

    public UnknownQualifier_c(TypeSystem ts, Position pos) {
        super(ts, pos);
    }

    public boolean isCanonical() { return false; }
    public boolean isPackage() { return false; }
    public boolean isType() { return false; }

    public Package toPackage() { return null; }
    public Type toType() { return null; }

    public String toString() {
        return "<unknown>";
    }
}
