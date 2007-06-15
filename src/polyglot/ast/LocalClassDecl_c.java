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
 * A local class declaration statement.  The node is just a wrapper around
 * a class declaration.
 */
public class LocalClassDecl_c extends Stmt_c implements LocalClassDecl
{
    protected ClassDecl decl;

    public LocalClassDecl_c(Position pos, ClassDecl decl) {
	super(pos);
	assert(decl != null);
	this.decl = decl;
    }

    /** Get the class declaration. */
    public ClassDecl decl() {
	return this.decl;
    }

    /** Set the class declaration. */
    public LocalClassDecl decl(ClassDecl decl) {
	LocalClassDecl_c n = (LocalClassDecl_c) copy();
	n.decl = decl;
	return n;
    }

    /** Reconstruct the statement. */
    protected LocalClassDecl_c reconstruct(ClassDecl decl) {
        if (decl != this.decl) {
	    LocalClassDecl_c n = (LocalClassDecl_c) copy();
	    n.decl = decl;
	    return n;
	}

	return this;
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term firstChild() {
        return decl();
    }

    /**
     * Visit this term in evaluation order.
     */
    public List acceptCFG(CFGBuilder v, List succs) {
        v.visitCFG(decl(), this, false);
        return succs;
    }

    /** Visit the children of the statement. */
    public Node visitChildren(NodeVisitor v) {
        ClassDecl decl = (ClassDecl) visitChild(this.decl, v);
        return reconstruct(decl);
    }

    public void addDecls(Context c) {
        // We should now be back in the scope of the enclosing block.
        // Add the type.
        if (! decl.type().toClass().isLocal())
            throw new InternalCompilerError("Non-local " + decl.type() +
                                            " found in method body.");
        c.addNamed(decl.type().toClass());
    }

    public String toString() {
	return decl.toString();
    }

    /** Write the statement to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printBlock(decl, w, tr);
	w.write(";");
    }
    
    public Node copy(NodeFactory nf) {
        return nf.LocalClassDecl(this.position, this.decl);
    }

}
