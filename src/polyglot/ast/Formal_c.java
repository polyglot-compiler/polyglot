/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import java.util.*;

/**
 * A <code>Formal</code> represents a formal parameter for a procedure
 * or catch block.  It consists of a type and a variable identifier.
 */
public class Formal_c extends Term_c implements Formal
{
    protected LocalInstance li;
    protected Flags flags;
    protected TypeNode type;
    protected Id name;
//    protected boolean reachable;

    public Formal_c(Position pos, Flags flags, TypeNode type,
                    Id name)
    {
	super(pos);
	assert(flags != null && type != null && name != null);
        this.flags = flags;
        this.type = type;
        this.name = name;
    }

    public boolean isDisambiguated() {
        return li != null && li.isCanonical() && super.isDisambiguated();
    }

    /** Get the type of the formal. */
    public Type declType() {
        return type.type();
    }

    /** Get the flags of the formal. */
    public Flags flags() {
	return flags;
    }

    /** Set the flags of the formal. */
    public Formal flags(Flags flags) {
        if (flags.equals(this.flags)) return this;
	Formal_c n = (Formal_c) copy();
	n.flags = flags;
	return n;
    }

    /** Get the type node of the formal. */
    public TypeNode type() {
	return type;
    }

    /** Set the type node of the formal. */
    public Formal type(TypeNode type) {
	Formal_c n = (Formal_c) copy();
	n.type = type;
	return n;
    }
    
    /** Get the name of the formal. */
    public Id id() {
        return name;
    }
    
    /** Set the name of the formal. */
    public Formal id(Id name) {
        Formal_c n = (Formal_c) copy();
        n.name = name;
        return n;
    }

    /** Get the name of the formal. */
    public String name() {
	return name.id();
    }

    /** Set the name of the formal. */
    public Formal name(String name) {
        return id(this.name.id(name));
    }

    /** Get the local instance of the formal. */
    public LocalInstance localInstance() {
        return li;
    }

    /** Set the local instance of the formal. */
    public Formal localInstance(LocalInstance li) {
        if (li == this.li) return this;
        Formal_c n = (Formal_c) copy();
	n.li = li;
	return n;
    }

    /** Reconstruct the formal. */
    protected Formal_c reconstruct(TypeNode type, Id name) {
	if (this.type != type) {
	    Formal_c n = (Formal_c) copy();
	    n.type = type;
            n.name = name;
	    return n;
	}

	return this;
    }

    /** Visit the children of the formal. */
    public Node visitChildren(NodeVisitor v) {
	TypeNode type = (TypeNode) visitChild(this.type, v);
        Id name = (Id) visitChild(this.name, v);
	return reconstruct(type, name);
    }

    public void addDecls(Context c) {
        c.addVariable(li);
    }

    /** Write the formal to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(flags.translate());
        print(type, w, tr);
        w.write(" ");
        tr.print(this, name, w);
    }

    /** Build type objects for the formal. */
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Formal_c n = (Formal_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        LocalInstance li = ts.localInstance(position(), flags(),
                                            ts.unknownType(position()), name());

        return n.localInstance(li);
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (type.isDisambiguated() && ! li.type().isCanonical()) {
            li.setFlags(flags());
            li.setType(declType());
            li.setNotConstant();
        }

        return this;
    }

    /** Type check the formal. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Check if the variable is multiply defined.
        Context c = tc.context();

        LocalInstance outerLocal = null;

        try {
            outerLocal = c.findLocal(li.name());
        }
        catch (SemanticException e) {
            // not found, so not multiply defined
        }

        if (outerLocal != null && outerLocal != li && c.isLocal(li.name())) {
            throw new SemanticException(
                "Local variable \"" + name + "\" multiply defined.  "
                    + "Previous definition at " + outerLocal.position() + ".",
                position());
        }

	TypeSystem ts = tc.typeSystem();

	try {
	    ts.checkLocalFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

	return this;
    }

    public Term firstChild() {
        return type;
    }

    public List acceptCFG(CFGBuilder v, List succs) {
        v.visitCFG(type, this, EXIT);        
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

	w.allowBreak(4, " ");
	w.begin(0);
	w.write("(name " + name + ")");
	w.end();
    }

    public String toString() {
        return flags.translate() + type + " " + name;
    }
    public Node copy(NodeFactory nf) {
        return nf.Formal(this.position, this.flags, this.type, this.name);
    }

}
