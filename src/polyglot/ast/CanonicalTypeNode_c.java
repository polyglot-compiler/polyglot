package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.frontend.Compiler;
import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.main.Options;

/**
 * A <code>TypeNode</code> represents the syntactic representation of a 
 * <code>Type</code> within the abstract syntax tree.
 */
public class CanonicalTypeNode_c extends TypeNode_c implements CanonicalTypeNode
{
    public CanonicalTypeNode_c(Ext ext, Position pos, Type type) {
	super(ext, pos);
	this.type = type;
    }

    /*
     * If the "use-fully-qualified-class-names" options is used, then the
     * fully qualified names is written out (<code>java.lang.Object</code>).
     * Otherwise, the string that originally represented the type in the
     * source file is used.
     */
    public void translate_(CodeWriter w, Translator tr) {
	TypeSystem ts = tr.typeSystem();

	if (! Options.global.fully_qualified_names) {
	    w.write(type.translate(tr.context()));
	}
	else {
	    w.write(type.translate(null));
	}
    }

    public String toString() {
        return type.toString();
    }
}
