package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>AmbReceiver</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a receiver.
 */
public class AmbReceiver_c extends AmbPrefix_c implements AmbReceiver
{
	protected Type type;

    public AmbReceiver_c(Ext ext, Position pos, Prefix prefix, String name) {
	super(ext, pos, prefix, name);
    }

	public Type type() {
		return this.type;
	}

	public AmbReceiver type(Type type) {
                AmbReceiver_c n = (AmbReceiver_c) copy();
		n.type = type;
                return n;
	}

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
        return type(tb.typeSystem().unknownType(position()));
    }

    /** Disambiguate the receiver. */
    public Node disambiguateOverride_(AmbiguityRemover ar)
	throws SemanticException {

	Node n = super.disambiguateOverride_(ar);

	if (n == null) {
	    return null;
	}

	if (n instanceof Receiver) {
	    return n;
	}

	throw new SemanticException("Could not find type, field, or " +
	    "local variable \"" + name + "\".", position());
    }

    /** Disambiguate the receiver. */
    public Node disambiguate_(AmbiguityRemover ar)
	throws SemanticException {

	Node n = super.disambiguate_(ar);

	if (n instanceof Receiver) {
	    return n;
	}

	throw new SemanticException("Could not find type, field, or " +
	    "local variable \"" + name + "\".", position());
    }
}
