package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;
import jltools.types.Package;

import java.io.*;
import java.util.*;

/** A Translator generates output code from the processed AST.
 */
public class Translator extends AbstractPass
{
    protected Job job;
    protected Context context;
    protected boolean appendSemicolon = true;

    /**
     * Create a Translator.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public Translator(Job job) {
	this.job = job;
    }

    public boolean appendSemicolon() {
        return appendSemicolon;
    }

    public boolean appendSemicolon(boolean a) {
        boolean old = this.appendSemicolon;
        this.appendSemicolon = a;
	return old;
    }

    public Context context() {
        return context;
    }

    public TypeSystem typeSystem() {
        return job.compiler().typeSystem();
    }

    public NodeFactory nodeFactory() {
        return job.compiler().nodeFactory();
    }

    public boolean run() {
	jltools.frontend.Compiler compiler = job.compiler();

	TypeSystem ts = compiler.typeSystem();
	NodeFactory nf = compiler.nodeFactory();
	TargetFactory tf = compiler.targetFactory();
	int outputWidth = compiler.outputWidth();
	Collection outputFiles = compiler.outputFiles();

	this.context = ts.createContext(job.importTable());

	SourceFile sfn = (SourceFile) job.ast();

	// Find the public declarations in the file.  We'll use these to
	// derive the names of the target files.  There will be one
	// target file per public declaration.  If there are no public
	// declarations, we'll use the source file name to derive the
	// target file name.
	List exports = exports(sfn);

	try {
	    File of;
	    Writer ofw;
	    CodeWriter w;

	    String pkg = "";

	    if (sfn.package_() != null) {
		Package p = sfn.package_().package_();
		pkg = p.toString();
	    }

	    TopLevelDecl first = null;

	    if (exports.size() == 0) {
		// Use the source name to derive a default output file name.
	    	of = tf.outputFile(pkg, job.source());
	    }
	    else {
		first = (TopLevelDecl) exports.get(0);
	    	of = tf.outputFile(pkg, first.name());
	    }
            
            String opfPath = of.getPath();
            if (!opfPath.endsWith("$")) outputFiles.add(of.getPath());
	    ofw = tf.outputWriter(of);
	    w = new CodeWriter(ofw, outputWidth);

	    writeHeader(sfn, w);

	    for (Iterator i = sfn.decls().iterator(); i.hasNext(); ) {
		TopLevelDecl decl = (TopLevelDecl) i.next();

		if (decl.flags().isPublic() && decl != first) {
		    // We hit a new exported declaration, open a new file.
		    // But, first close the old file.
		    w.flush();
		    ofw.close();

		    of = tf.outputFile(pkg, decl.name());
		    outputFiles.add(of.getPath());
		    ofw = tf.outputWriter(of);
		    w = new CodeWriter(ofw, outputWidth);

		    writeHeader(sfn, w);
		}

		decl.ext().translate(w, this);
		w.newline(0);

		if (i.hasNext()) {
		    w.newline(0);
		}
	    }

	    w.flush();
	    ofw.close();
	    return true;
	}
	catch (IOException e) {
	    job.compiler().errorQueue().enqueue(ErrorInfo.IO_ERROR,
		       "I/O error while translating: " + e.getMessage());
	    return false;
	}
    }

    protected void writeHeader(SourceFile sfn, CodeWriter w) {
	if (sfn.package_() != null) {
	    w.write("package ");
	    sfn.package_().ext().translate(w, this);
	    w.write(";");
	    w.newline(0);
	    w.newline(0);
	}

	boolean newline = false;

	for (Iterator i = sfn.imports().iterator(); i.hasNext(); ) {
	    Import imp = (Import) i.next();
	    imp.ext().translate(w, this);
	    newline = true;
	}
		   
	if (newline) {
	    w.newline(0);
	}
    }

    protected List exports(SourceFile sfn) {
	List exports = new LinkedList();

	for (Iterator i = sfn.decls().iterator(); i.hasNext(); ) {
	    TopLevelDecl decl = (TopLevelDecl) i.next();

	    if (decl.flags().isPublic()) {
		exports.add(decl);
	    }
	}

	return exports;
    }

    public String toString() {
	return "Translator(" + job + ")";
    }
}
