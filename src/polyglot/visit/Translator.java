package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.types.Package;
import polyglot.frontend.Compiler;

import java.io.*;
import java.util.*;

/**
 * A Translator generates output code from the processed AST.
 *
 * To use:
 *     new Translator(job, ts, nf, tf).translate(ast);
 */
public class Translator extends PrettyPrinter
{
    protected Job job;
    protected NodeFactory nf;
    protected TargetFactory tf;
    protected TypeSystem ts;
    protected Context context;
    protected ClassType outerClass = null;

    /**
     * Create a Translator.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public Translator(Job job, TypeSystem ts, NodeFactory nf, TargetFactory tf) {
        super();

        this.job = job;
        this.nf = nf;
        this.tf = tf;
        this.ts = ts;
        this.context = job.context();

        if (this.context == null) {
            this.context = ts.createContext();
        }
    }

    public ClassType outerClass() {
        return outerClass;
    }

    public void setOuterClass(ClassType ct) {
        this.outerClass = ct;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

    public Context context() {
        return context;
    }

    public NodeFactory nodeFactory() {
        return nf;
    }

    public void print(Node ast, CodeWriter w) {
        ast.del().translate(w, this);
    }

    public boolean translate(Node ast) {
        if (ast instanceof SourceFile) {
            SourceFile sfn = (SourceFile) ast;
            return translateSource(sfn);
        }
        else if (ast instanceof SourceCollection) {
            SourceCollection sc = (SourceCollection) ast;

            boolean okay = true;

            for (Iterator i = sc.sources().iterator(); i.hasNext(); ) {
                SourceFile sfn = (SourceFile) i.next();
                okay &= translateSource(sfn);
            }

            return okay;
        }
        else {
            throw new InternalCompilerError("AST root must be a SourceFile; " +
                                            "found a " + ast.getClass().getName());
        }
    }

    protected boolean translateSource(SourceFile sfn) {
        TypeSystem ts = typeSystem();
        NodeFactory nf = nodeFactory();
	TargetFactory tf = this.tf;
	int outputWidth = job.compiler().outputWidth();
	Collection outputFiles = job.compiler().outputFiles();

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

            sfn.enterScope(context);

            TopLevelDecl first = null;

            if (exports.size() == 0) {
                // Use the source name to derive a default output file name.
                of = tf.outputFile(pkg, sfn.source());
            }
            else {
                first = (TopLevelDecl) exports.get(0);
                of = tf.outputFile(pkg, first.name(), sfn.source());
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

                    of = tf.outputFile(pkg, decl.name(), sfn.source());
                    outputFiles.add(of.getPath());
                    ofw = tf.outputWriter(of);
                    w = new CodeWriter(ofw, outputWidth);

                    writeHeader(sfn, w);
                }

                decl.del().translate(w, this);

                if (i.hasNext()) {
                    w.newline(0);
                }
            }

            sfn.leaveScope(context);

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
	    sfn.package_().del().translate(w, this);
	    w.write(";");
	    w.newline(0);
	    w.newline(0);
	}

	boolean newline = false;

	for (Iterator i = sfn.imports().iterator(); i.hasNext(); ) {
	    Import imp = (Import) i.next();
	    imp.del().translate(w, this);
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
	return "Translator";
    }
}
