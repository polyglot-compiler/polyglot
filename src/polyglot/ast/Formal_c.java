package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

/**
 * A <code>Formal</code> is immutable representation of a ordered 
 * pair: a type and a variable declarator identifier, used as formal
 * parameters such as in method declarations and catch blocks.
 */
public class Formal_c extends Node_c implements Formal
{
    Declarator_c decl;
    LocalInstance li;

    public Formal_c(Ext ext, Position pos, Flags flags, TypeNode type, String name) {
	super(ext, pos);
	this.decl = new Declarator_c(flags, type, name, null);
    }

    public Type declType() {
        return decl.declType();
    }

    public Flags flags() {
	return decl.flags();
    }

    public Formal flags(Flags flags) {
	Formal_c n = (Formal_c) copy();
	n.decl = decl.flags(flags);
	return n;
    }

    public TypeNode type() {
	return decl.type();
    }

    public Formal type(TypeNode type) {
	Formal_c n = (Formal_c) copy();
	n.decl = decl.type(type);
	return n;
    }

    public String name() {
	return decl.name();
    }

    public Formal name(String name) {
	Formal_c n = (Formal_c) copy();
	n.decl = decl.name(name);
	return n;
    }

    public LocalInstance localInstance() {
        return li;
    }

    public Formal localInstance(LocalInstance li) {
        Formal_c n = (Formal_c) copy();
	n.li = li;
	return n;
    }

    protected Formal_c reconstruct(TypeNode type) {
	if (type() != type) {
	    Formal_c n = (Formal_c) copy();
	    n.decl = (Declarator_c) decl.copy();
	    n.decl = n.decl.type(type);
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	TypeNode type = (TypeNode) type().visit(v);
	return reconstruct(type);
    }

    public void leaveScope(Context c) {
	c.addVariable(li);
    }

    public void translate_(CodeWriter w, Translator tr) {
        decl.translate(w, tr, false);
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
	TypeSystem ts = tb.typeSystem();
	LocalInstance li = ts.localInstance(position(), flags(),
		                            declType(), name());
	return localInstance(li);
    }

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

    public Node reconstructTypes_(NodeFactory nf, TypeSystem ts, Context c)
        throws SemanticException {

	Flags flags = flags();
        Type type = declType();
        String name = name();

	LocalInstance li = this.li;

	if (! flags.equals(li.flags())) li = li.flags(flags);
	if (! type.equals(li.type())) li = li.type(type);
	if (! name.equals(li.name())) li = li.name(name);
        
	if (li != this.li) {
	    return localInstance(li);
	}

	return this;
    }
}
