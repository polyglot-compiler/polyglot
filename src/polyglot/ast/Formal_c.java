package polyglot.ext.jl.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/**
 * A <code>Formal</code> represents a formal parameter to a method
 * or constructor or to a catch block.  It consists of a type and a variable
 * identifier.
 */
public class Formal_c extends Node_c implements Formal
{
    Declarator decl;
    LocalInstance li;

    public Formal_c(Del ext, Position pos, Flags flags, TypeNode type, String name) {
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

    public Context enterScope(Context c) {
        c.addVariable(li);
        return c;
    }

    /** Write the formal to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        decl.prettyPrint(w, tr, false);
    }

    /** Build type objects for the formal. */
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Formal_c n = (Formal_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        LocalInstance li = ts.localInstance(position(), Flags.NONE,
                                            ts.unknownType(position()), name());

        return n.localInstance(li);
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (declType().isCanonical() && ! li.type().isCanonical()) {
            TypeSystem ts = ar.typeSystem();
            LocalInstance li = ts.localInstance(position(), flags(),
                                                declType(), name());
            return localInstance(li);
        }

        return this;
    }

    /** Type check the formal. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
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

    public Computation entry() {
        return this;
    }

    public List acceptCFG(CFGBuilder v, List succs) {
        return succs;
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
