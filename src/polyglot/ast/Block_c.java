/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/**
 * A <code>Block</code> represents a Java block statement -- an immutable
 * sequence of statements.
 */
public class Block_c extends AbstractBlock_c implements Block
{
    public Block_c(Position pos, List statements) {
	super(pos, statements);
    }

    /** Write the block to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("{");
	w.unifiedBreak(4, 1, " ", 1);
	w.begin(0);
	super.prettyPrint(w, tr);
	w.end();
	w.unifiedBreak(0, 1, " ", 1);
	w.write("}");
    }
    public Node copy(NodeFactory nf) {
        return nf.Block(this.position, this.statements);
    }
}
