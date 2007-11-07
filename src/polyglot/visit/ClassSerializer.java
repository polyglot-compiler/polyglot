/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import java.io.IOException;
import java.util.*;

import polyglot.ast.*;
import polyglot.main.Report;
import polyglot.main.Version;
import polyglot.types.*;
import polyglot.util.*;

/**
 * Visitor which serializes class objects and adds a field to the class
 * containing the serialization.
 */
public class ClassSerializer extends NodeVisitor
{
    /**
     * The maximum number of characters that will be assigned to an encoded type string field.
     * More characters than this will be broken up over several fields.
     */
    private static final int MAX_ENCODED_TYPE_INFO_STRING_LENGTH = 8192;
    
    protected TypeEncoder te;
    protected ErrorQueue eq;
    protected Date date;
    protected TypeSystem ts;
    protected NodeFactory nf;
    protected Version ver;

    public ClassSerializer(TypeSystem ts, NodeFactory nf, Date date, ErrorQueue eq, Version ver) {
        this.ts = ts;
        this.nf = nf;
        this.te = new TypeEncoder(ts);
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

        ClassDecl cd = (ClassDecl) n;
        ClassBody body = cd.body();

        List l = createSerializationMembers(cd);

        for (Iterator i = l.iterator(); i.hasNext(); ) {
            ClassMember m = (ClassMember) i.next();
	    body = body.addMember(m);
        }

        return cd.body(body);
    }

    public List createSerializationMembers(ClassDecl cd) {
        return createSerializationMembers(cd.type());
    }
    
    public List createSerializationMembers(ClassType ct) {
	try {
	    byte[] b;
            List newMembers = new ArrayList(3);

            // HACK: force class members to get created from lazy class
            // initializer.
            ct.memberClasses();
            ct.constructors();
            ct.methods();
            ct.fields();
            ct.interfaces();
            ct.superType();

            // Only serialize top-level and member classes.
	    if (! ct.isTopLevel() && ! ct.isMember()) {
                return Collections.EMPTY_LIST;
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

		return Collections.EMPTY_LIST;
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
            fi.setConstantValue(version);
            ii = ts.initializerInstance(pos, ct, Flags.STATIC);
	    f = nf.FieldDecl(fi.position(), fi.flags(),
		             nf.CanonicalTypeNode(fi.position(), fi.type()),
                             nf.Id(fi.position(), fi.name()),
			     nf.StringLit(pos, version).type(ts.String()));

	    f = f.fieldInstance(fi);
            f = f.initializerInstance(ii);
            newMembers.add(f);

	    /* Add the date of the last source file modification. */
	    long time = date.getTime();

	    fi = ts.fieldInstance(pos, ct,
                                  flags, ts.Long(),
                                  "jlc$SourceLastModified$" + suffix);
            fi.setConstantValue(new Long(time));
            ii = ts.initializerInstance(pos, ct, Flags.STATIC);
	    f = nf.FieldDecl(fi.position(), fi.flags(),
		             nf.CanonicalTypeNode(fi.position(), fi.type()),
                             nf.Id(fi.position(), fi.name()),
			     nf.IntLit(pos, IntLit.LONG, time).type(ts.Long()));

	    f = f.fieldInstance(fi);
            f = f.initializerInstance(ii);
            newMembers.add(f);

            // output the encoded type info, over several fields if needed.
            String encodedTypeInfo = te.encode(ct);
            int etiStart = 0;
            int etiEnd = 0;
            int numberETIFields = 0;
            do {
                etiEnd = encodedTypeInfo.length();
                if (etiEnd - etiStart > MAX_ENCODED_TYPE_INFO_STRING_LENGTH) {
                    etiEnd = etiStart + MAX_ENCODED_TYPE_INFO_STRING_LENGTH;
                }
                // add an additional suffix to distinguish fields.
                String additionalFieldSuffix = numberETIFields==0?"":("$" + numberETIFields);
                String encoded = encodedTypeInfo.substring(etiStart, etiEnd);
                fi = ts.fieldInstance(pos, ct,
                                      flags, ts.String(),
                                      "jlc$ClassType$" + suffix + additionalFieldSuffix);
                fi.setConstantValue(encoded);
                ii = ts.initializerInstance(pos, ct, Flags.STATIC);

                f = nf.FieldDecl(fi.position(), fi.flags(),
                                 nf.CanonicalTypeNode(fi.position(), fi.type()),
                                 nf.Id(fi.position(), fi.name()),
                                 nf.StringLit(pos, encoded).type(ts.String()));

                f = f.fieldInstance(fi);
                f = f.initializerInstance(ii);
                newMembers.add(f);
                
                numberETIFields++;
                etiStart = etiEnd;
            }
            while (etiEnd != encodedTypeInfo.length());
            
            return newMembers;
	}
	catch (IOException e) {
            if (Report.should_report(Report.serialize, 1))
                e.printStackTrace();
	    eq.enqueue(ErrorInfo.IO_ERROR,
		       "Unable to serialize class information.");
            return Collections.EMPTY_LIST;
	}
    }
}
