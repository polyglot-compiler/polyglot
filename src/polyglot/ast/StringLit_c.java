package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/** 
 * A <code>StringLit</code> represents an immutable instance of a 
 * <code>String</code> which corresponds to a literal string in Java code.
 */
public class StringLit_c extends Lit_c implements StringLit
{
    protected String value;

    public StringLit_c(Del ext, Position pos, String value) {
	super(ext, pos);
	this.value = value;
    }

    /** Get the value of the expression. */
    public String value() {
	return this.value;
    }

    /** Set the value of the expression. */
    public StringLit value(String value) {
	StringLit_c n = (StringLit_c) copy();
	n.value = value;
	return n;
    }

    /** Get the value of the expression, as an object. */
    public Object objValue() {
	return this.value;
    }

    /** Type check the expression. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().String());
    }

    public String toString() {
        if (StringUtil.unicodeEscape(value).length() > 11) {
            return "\"" + StringUtil.unicodeEscape(value.substring(0,8)) + "...\"";
        }
                
	return "\"" + StringUtil.unicodeEscape(value) + "\"";
    }

    protected int MAX_LENGTH = 60;
 
    /** Write the expression to an output file. */
    public void translate(CodeWriter w, Translator tr) {
        if (StringUtil.unicodeEscape(value).length() > MAX_LENGTH) {
            tr.print(breakupString(tr.nodeFactory(), tr.typeSystem()), w);
        }
        else {
            w.write("\"");
            w.write(StringUtil.escape(value));
            w.write("\"");
        }
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("\"");

        if (StringUtil.unicodeEscape(value).length() > 11) {
            w.write(StringUtil.escape(value.substring(0,8) + "..."));
        }
        else {
            w.write(StringUtil.escape(value));
        }

        w.write("\"");
    }

    /**
     * Break a long string literal into a concatenation of small string
     * literals.  This avoids messing up the pretty printer and editors. 
     */
    protected Expr breakupString(NodeFactory nf, TypeSystem ts) {
        Expr result = null;
        int n = value.length();
        int i = 0;

        for (;;) {
            int j;

            // Compensate for the unicode transformation by computing
            // the length of the encoded string (or something close to it).
            int len = 0;

            for (j = i; j < n; j++) {
                char c = value.charAt(j);
                int k = StringUtil.unicodeEscape(c).length();
                if (len + k > MAX_LENGTH) break;
                len += k;
            }

            StringLit s = nf.StringLit(position(), value.substring(i, j));
            s = (StringLit) s.type(ts.String());

            // Check that we don't create a string that's too long.
            // Otherwise we'll infinitely recurse.
            if (StringUtil.unicodeEscape(s.value()).length() > MAX_LENGTH) {
                throw new InternalCompilerError("Max string length exceeded.");
            }

            if (result == null) {
                result = s;
            }
            else {
                result = nf.Binary(position(), result, Binary.ADD, s);
                result = result.type(ts.String());
            }

            if (j == n)
                return result;

            i = j;
        }
    }
}
