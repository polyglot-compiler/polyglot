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
    protected Version ver;

    public ClassSerializer(TypeSystem ts, NodeFactory nf, Date date, ErrorQueue eq, Version ver) {
	this.ts = ts;
	this.nf = nf;
	this.te = new TypeEncoder( ts);
	this.eq = eq;
	this.date = date;
        this.ver = ver;
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

	    /* Add the compiler version number. */
            String suffix = ver.name();

	    // Check if we've already serialized.
	    if (ct.fieldNamed("jlc$CompilerVersion$" + suffix) != null ||
		ct.fieldNamed("jlc$SourceLastModified$" + suffix) != null ||
		ct.fieldNamed("jlc$ClassType$" + suffix) != null) {

		eq.enqueue(ErrorInfo.SEMANTIC_ERROR,
			   "Cannot serialize class information " +
			   "more than once.");

		return n;
	    }

	    Flags flags = Flags.PUBLIC.set(Flags.STATIC).set(Flags.FINAL);

	    FieldDecl f;
            FieldInstance fi;
            InitializerInstance ii;

	    /* Add the compiler version number. */
	    String version = ver.major() + "." +
			     ver.minor() + "." +
			     ver.patch_level();

            Position pos = Position.COMPILER_GENERATED;

	    fi = ts.fieldInstance(pos, ct,
                                  flags, ts.String(),
                                  "jlc$CompilerVersion$" + suffix);
            ii = ts.initializerInstance(pos, ct, Flags.STATIC);
	    f = nf.FieldDecl(fi.position(), fi.flags(),
		             nf.CanonicalTypeNode(fi.position(), fi.type()),
			     fi.name(),
			     nf.StringLit(pos, version).type(ts.String()));

	    f = f.fieldInstance(fi);
            f = f.initializerInstance(ii);
	    body = body.addMember(f);

	    /* Add the date of the last source file modification. */
	    long time = date.getTime();

	    fi = ts.fieldInstance(pos, ct,
                                  flags, ts.Long(),
                                  "jlc$SourceLastModified$" + suffix);
            ii = ts.initializerInstance(pos, ct, Flags.STATIC);
	    f = nf.FieldDecl(fi.position(), fi.flags(),
		             nf.CanonicalTypeNode(fi.position(), fi.type()),
			     fi.name(),
			     nf.IntLit(pos, time).type(ts.Long()));

	    f = f.fieldInstance(fi);
            f = f.initializerInstance(ii);
	    body = body.addMember(f);

	    /* Add the class type info. */
	    fi = ts.fieldInstance(pos, ct,
                                  flags, ts.String(),
                                  "jlc$ClassType$" + suffix);
            ii = ts.initializerInstance(pos, ct, Flags.STATIC);
	    f = nf.FieldDecl(fi.position(), fi.flags(),
		             nf.CanonicalTypeNode(fi.position(), fi.type()),
			     fi.name(),
			     largeStringLiteral(te.encode(ct)).type(ts.String()));

	    f = f.fieldInstance(fi);
            f = f.initializerInstance(ii);
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

	    Expr s = nf.StringLit(Position.COMPILER_GENERATED,
                                  x.substring(i, j)).type(ts.String());

	    if (result == null) {
		result = s;
	    }
	    else {
		result = nf.Binary(Position.COMPILER_GENERATED,
                                   result, Binary.ADD, s).type(ts.String());
	    }

	    if (j == n)
		return result;

	    i = j;
	}
    }
}
