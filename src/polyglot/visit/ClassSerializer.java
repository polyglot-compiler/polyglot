package jltools.visit;

import jltools.main.*;
import jltools.ast.*;
import jltools.frontend.Compiler;
import jltools.types.*;
import jltools.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Visitor which serializes class objects and adds a field to the class
 * containing the serialization.
 */
public class ClassSerializer extends NodeVisitor
{
    protected TypeEncoder te;
    protected ErrorQueue eq;
    protected Date date;
    protected TypeSystem ts;
    protected NodeFactory nf;

    public ClassSerializer(TypeSystem ts, NodeFactory nf, Date date, ErrorQueue eq) {
	this.ts = ts;
	this.nf = nf;
	this.te = new TypeEncoder( ts);
	this.eq = eq;
	this.date = date;
    }

    public Node override(Node n) {
        // Stop at class members.  We only want to encode top-level classes.
	if (n instanceof ClassMember && ! (n instanceof ClassDecl)) {
	    return n;
	}

	return null;
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	if (! (n instanceof ClassDecl)) {
	    return n;
	}

	try {
	    ClassDecl cn = (ClassDecl) n;
	    ClassBody body = cn.body();
	    ParsedClassType ct = cn.type();
	    byte[] b;

	    if (! ct.isTopLevel()) {
	        return n;
	    }

	    // Check if we've already serialized.
	    if (ct.fieldNamed("jlc$CompilerVersion") != null ||
		ct.fieldNamed("jlc$SourceLastModified") != null ||
		ct.fieldNamed("jlc$ClassType") != null) {

		eq.enqueue(ErrorInfo.SEMANTIC_ERROR,
			   "Cannot serialize class information " +
			   "more than once.");

		return n;
	    }

	    Flags flags = Flags.PUBLIC.set(Flags.STATIC).set(Flags.FINAL);

	    FieldDecl f;

	    /* Add the compiler version number. */
            Version ver = Options.global.extension.version();
	    String version = ver.major() + "." +
			     ver.minor() + "." +
			     ver.patch_level();

	    f = nf.FieldDecl(null, flags,
		             nf.CanonicalTypeNode(null, ts.String()),
			     "jlc$CompilerVersion",
			     nf.StringLit(null, version).type(ts.String()));

	    f = f.fieldInstance(ts.fieldInstance(null, ct,
						 flags, ts.String(),
						 "jlc$CompilerVersion"));
	    body = body.addMember(f);

	    /* Add the date of the last source file modification. */
	    long time = date.getTime();

	    f = nf.FieldDecl(null, flags,
		             nf.CanonicalTypeNode(null, ts.Long()),
			     "jlc$SourceLastModified",
			     nf.IntLit(null, time).type(ts.Long()));
	    f = f.fieldInstance(ts.fieldInstance(null, ct,
						 flags, ts.Long(),
						 "jlc$SourceLastModified"));
	    body = body.addMember(f);

	    /* Add the class type info. */
	    f = nf.FieldDecl(null, flags,
		             nf.CanonicalTypeNode(null, ts.String()),
			     "jlc$ClassType",
			     largeStringLiteral(te.encode(ct)));
	    f = f.fieldInstance(ts.fieldInstance(null, ct,
						 flags, ts.String(),
						 "jlc$ClassType"));
	    body = body.addMember(f);

	    return cn.body(body);
	}
	catch (IOException e) {
	    eq.enqueue(ErrorInfo.IO_ERROR,
		       "Unable to serialize class information.");
	    return n;
	}
    }

    /**
     * Break a long string literal into a sum of small string literals.
     * This avoids messing up the pretty printer and editors. However, it
     * does not entirely solve the formatting problem if the pretty-printer
     * output is post-processed by a unicode transformation (which it is),
     * since the pretty-printer doesn't realize that the unicode characters
     * expand to multiple characters.
     */
    private final Expr largeStringLiteral(String x) {
	Expr result = null;
	int n = x.length();
	int i = 0;

	for (;;) {
	    int j;
	    // Compensate for the unicode transformation by computing
	    // the length of the encoded string (or something close to it).
	    int len = 0;
	    for (j = i; len < 60 && j < n; j++) {
		if (x.charAt(j) > 0xff) len += 6;
		len += StringUtil.escape(x.charAt(j)).length();
	    }

	    Expr s = nf.StringLit(null, x.substring(i, j)).type(ts.String());

	    if (result == null) {
		result = s;
	    }
	    else {
		result = nf.Binary(null, result, Binary.ADD, s).type(ts.String());
	    }

	    if (j == n)
		return result;

	    i = j;
	}
    }
}
