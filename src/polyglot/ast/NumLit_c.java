package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * class for the abstraction of chars and ints, longs, bytes and short literals.
 */
public class NumLit_c extends Lit_c implements NumLit
{
    protected long value;

    public NumLit_c(Ext ext, Position pos, long value) {
	super(ext, pos);
	this.value = value;
    }

    public long longValue() {
	return this.value;
    }

    public Object objValue() {
        return new Long(this.value);
    }
}
