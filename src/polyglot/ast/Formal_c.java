package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

/**
 * A <code>Formal</code> represents a formal parameter to a method
 * or constructor or to a catch block.  It consists of a type and a variable
 * identifier.
 */
public class Formal_c extends Node_c implements Formal
{
    Declarator decl;
    LocalInstance li;

    public Formal_c(Ext ext, Position pos, Flags flags, TypeNode type, String name) {
	super(ext, pos);
	this.decl = new Declarator_c(flags, type, name, null);
    }

    /** Get the type of the formal. */
    public Type declType() {
        return decl.declType();
    }

    /** Get the flags of the formal. */
    public Flags flags() {
	return decl.flags();
    }

    /** Set the flags of the formal. */
    public Formal flags(Flags flags) {
	Formal_c n = (Formal_c) copy();
	n.decl = decl.flags(flags);
	return n;
    }

    /** Get the type node of the formal. */
    public TypeNode type() {
	return decl.type();
    }

    /** Set the type node of the formal. */
    public Formal type(TypeNode type) {
	Formal_c n = (Formal_c) copy();
	n.decl = decl.type(type);
	return n;
    }

    /** Get the name of the formal. */
    public String name() {
	return decl.name();
    }

    /** Set the name of the formal. */
    public Formal name(String name) {
	Formal_c n = (Formal_c) copy();
	n.decl = decl.name(name);
	return n;
    }

    /** Get the local instance of the formal. */
    public LocalInstance localInstance() {
        return li;
    }

    /** Set the local instance of the formal. */
    public Formal localInstance(LocalInstance li) {
        Formal_c n = (Formal_c) copy();
	n.li = li;
	return n;
    }

    /** Reconstruct the formal. */
    protected Formal_c reconstruct(TypeNode type) {
	if (type() != type) {
	    Formal_c n = (Formal_c) copy();
	    n.decl = (Declarator_c) decl.copy();
	    n.decl = n.decl.type(type);
	    return n;
	}

	return this;
    }

    /** Visit the children of the formal. */
    public Node visitChildren(NodeVisitor v) {
	TypeNode type = (TypeNode) visitChild(type(), v);
	return reconstruct(type);
    }

    public void enterScope(Context c) {
    }

    public void leaveScope(Context c) {
        if (li != null) {
            c.addVariable(li);
        }
    }

    /** Write the formal to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        decl.translate(w, tr, false);
    }

    /** Build type objects for the formal. */
    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
        Formal_c n = (Formal_c) super.buildTypes_(tb);

        TypeSystem ts = tb.typeSystem();

        LocalInstance li = ts.localInstance(position(), Flags.NONE,
                                            ts.unknownType(position()), name());

        return n.localInstance(li);
    }

    public Node disambiguate_(AmbiguityRemover ar) throws SemanticException {
        if (declType().isCanonical() && ! li.type().isCanonical()) {
            TypeSystem ts = ar.typeSystem();
            LocalInstance li = ts.localInstance(position(), flags(),
                                                declType(), name());
            return localInstance(li);
        }

        return this;
    }

    /** Type check the formal. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	try {
	    ts.checkLocalFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

	decl.typeCheck(tc);

	return this;
    }

    public void dump(CodeWriter w) {
	super.dump(w);

	if (li != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(instance " + li + ")");
	    w.end();
	}
    }

    public String toString() {
	return decl.toString();
    }
}
