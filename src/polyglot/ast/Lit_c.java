package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * Lit
 *
 * Overview: An Lit represents any Java Lit.
 **/
public abstract class Lit_c extends Expr_c implements Lit
{
    public Lit_c(Ext ext, Position pos) {
	super(ext, pos);
    }

    public abstract Object objValue();

    public Precedence precedence() {
        return Precedence.LITERAL;
    }
}
