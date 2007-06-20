/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.visit.*;
import polyglot.util.*;
import java.util.*;

/**
 * <code>Empty</code> is the class for a empty statement <code>(;)</code>.
 */
public class Empty_c extends Stmt_c implements Empty
{
    public Empty_c(Position pos) {
	super(pos);
    }

    /** Write the statement to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write(";");
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term entry() {
        return this;
    }

    /**
     * Visit this term in evaluation order.
     */
    public List acceptCFG(CFGBuilder v, List succs) {
        return succs;
    }

    public String toString() {
	return ";";
    }
    public Node copy(NodeFactory nf) {
        return nf.Empty(this.position);
    }

}
