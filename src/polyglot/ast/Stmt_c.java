package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * A <code>Stmt</code> represents any Java statement.  All statements must
 * be subtypes of Statement.
 */
public abstract class Stmt_c extends Computation_c implements Stmt
{
    public Stmt_c(Del ext, Position pos) {
	super(ext, pos);
    }
}
