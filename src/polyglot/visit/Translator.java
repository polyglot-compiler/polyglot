package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;
import jltools.main.Main;

import java.io.*;
import java.util.*;

/** A TranslationVisitor generates output code from the processed AST.
 */
public class Translator implements Pass
{
    protected Job job;

    /**
     * Create a Translator.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public Translator(Job job) {
	this.job = job;
    }

    public boolean run() {
	jltools.frontend.Compiler compiler = job.compiler();

	TypeSystem ts = compiler.typeSystem();
	ExtensionFactory ef = compiler.extensionFactory();
	TargetFactory tf = compiler.targetFactory();
	int outputWidth = compiler.outputWidth();
	Collection outputFiles = compiler.outputFiles();

	// FIXME: need to make LocalContext take a Pass rather
	// than a Visitor.
	LocalContext c = ts.getLocalContext(job.importTable(), ef, this);

	SourceFileNode sfn = (SourceFileNode) job.ast();

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

	    String pkg = sfn.getPackageName();

	    GlobalDeclaration first = null;

	    if (exports.size() == 0) {
		// Use the source name to derive a default output file name.
	    	of = tf.outputFile(pkg, job.source());
	    }
	    else {
		first = (GlobalDeclaration) exports.get(0);
	    	of = tf.outputFile(pkg, first.getName());
	    }

	    outputFiles.add(of.getPath());
	    ofw = tf.outputWriter(of);
	    w = new CodeWriter(ofw, outputWidth);

	    writeHeader(sfn, c, w);

	    for (Iterator i = sfn.declarations(); i.hasNext(); ) {
		GlobalDeclaration decl = (GlobalDeclaration) i.next();

		if (decl.getAccessFlags().isPublic() && decl != first) {
		    // We hit a new exported declaration, open a new file.
		    // But, first close the old file.
		    w.flush();
		    ofw.close();

		    of = tf.outputFile(pkg, decl.getName());
		    outputFiles.add(of.getPath());
		    ofw = tf.outputWriter(of);
		    w = new CodeWriter(ofw, outputWidth);

		    writeHeader(sfn, c, w);
		}

		writeDecl(decl, c, w);
	    }

	    w.flush();
	    ofw.close();
	}
	catch (IOException e) {
	    job.compiler().errorQueue().enqueue(ErrorInfo.IO_ERROR,
		       "I/O error while translating: " + e.getMessage());
	    return false;
	}

	return true;
    }

    void writeHeader(SourceFileNode sfn, LocalContext c, CodeWriter w) {
	if (sfn.getPackageName() != null && ! sfn.getPackageName().equals("")) {
	    w.write("package " + sfn.getPackageName() + ";");
	    w.newline(0);
	    w.newline(0);
	}

	boolean newline = false;

	for (Iterator i = sfn.importNodes(); i.hasNext(); ) {
	    ImportNode imp = (ImportNode) i.next();
	    imp.translate(c, w);
	    newline = true;
	}
		   
	if (newline) {
	    w.newline(0);
	}
    }

    void writeDecl(GlobalDeclaration decl, LocalContext c, CodeWriter w) {
	((Node) decl).translate(c, w);
    }

    List exports(SourceFileNode sfn) {
	List exports = new LinkedList();

	for (Iterator i = sfn.declarations(); i.hasNext(); ) {
	    GlobalDeclaration decl = (GlobalDeclaration) i.next();

	    if (decl.getAccessFlags().isPublic()) {
		exports.add(decl);
	    }
	}

	return exports;
    }
}
