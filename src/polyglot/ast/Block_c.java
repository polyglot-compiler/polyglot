package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;
import java.util.*;

/**
 * A <code>Block</code> represents a Java block statement -- an immutable
 * sequence of statements.
 */
public class Block_c extends AbstractBlock_c implements Block
{
    public Block_c(Del ext, Position pos, List statements) {
	super(ext, pos, statements);
    }

    public String toString() {
	return "{ ... }";
    }

    /** Write the block to an output file. */
    public void translate(CodeWriter w, Translator tr) {
	w.write("{");
	w.allowBreak(4," ");

	super.translate(w, tr);

	w.allowBreak(0, " ");
	w.write("}");
    }
}
