package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/** 
 * An <code>CharLit</code> represents a literal in java of
 * <code>char</code> type.
 */
public class CharLit_c extends NumLit_c implements CharLit
{
    public CharLit_c(Ext ext, Position pos, char value) {
	super(ext, pos, value);
    }

    /** Get the value of the expression. */
    public char value() {
	return (char) longValue();
    }

    /** Set the value of the expression. */
    public CharLit value(char value) {
	CharLit_c n = (CharLit_c) copy();
	n.value = value;
	return n;
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	return type(tc.typeSystem().Char());
    }  

    public String toString() {
        return "'" + StringUtil.escape((char) value) + "'";
    }

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        w.write("'");
	w.write(StringUtil.escape((char) value));
        w.write("'");
    }
}
