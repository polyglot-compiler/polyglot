package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An integer literal: longs, ints, shorts, bytes, and chars.
 */
public abstract class NumLit_c extends Lit_c implements NumLit
{
    protected long value;

    public NumLit_c(Del ext, Position pos, long value) {
	super(ext, pos);
	this.value = value;
    }

    /** Get the value of the expression. */
    public long longValue() {
	return this.value;
    }

    /** Get the value of the expression, as an object. */
    public Object objValue() {
        return new Long(this.value);
    }
}
