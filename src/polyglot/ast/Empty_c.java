package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * <code>Empty</code> is the class for a empty statement <code>(;)</code>.
 */
public class Empty_c extends Stmt_c implements Empty
{
    public Empty_c(Del ext, Position pos) {
	super(ext, pos);
    }

    /** Write the statement to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write(";");
    }

    public String toString() {
	return ";";
    }
}
