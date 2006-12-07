package polyglot.visit;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceCollection;
import polyglot.ast.SourceFile;
import polyglot.ast.TopLevelDecl;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.Package;
import polyglot.types.TypeSystem;
import polyglot.util.*;
import polyglot.util.Copy;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;

/**
 * A Translator generates output code from the processed AST.
 * Output is sent to one or more java file in the directory
 * <code>Options.output_directory</code>.  Each SourceFile in the AST
 * is output to exactly one java file.  The name of that file is
 * determined as follows:
 * <ul>
 * <li> If the SourceFile has a declaration of a public top-level class "C",
 * file name is "C.java".  It is an error for there to be more than one
 * top-level public declaration.
 * <li> If the SourceFile has no public declarations, the file name
 * is the input file name (e.g., "X.jl") with the suffix replaced with ".java"
 * (thus, "X.java").
 * </ul>
 *
 * To use:
 * <pre>
 *     new Translator(job, ts, nf, tf).translate(ast);
 * </pre>
 * The <code>ast</code> must be either a SourceFile or a SourceCollection.
 */
public class TypedTranslator extends Translator
{
    protected Context context;

    /**
     * Create a Translator.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public TypedTranslator(Job job, TypeSystem ts, NodeFactory nf, TargetFactory tf) {
        super(job, ts, nf, tf);
        this.context = ts.createContext();
    }
    
    /** Create a new <code>Translator</code> identical to <code>this</code> but
     * with new context <code>c</code> */
    public TypedTranslator context(Context c) {
        if (c == this.context) {
            return this;
        }
        TypedTranslator tr = (TypedTranslator) copy();
        tr.context = c;
        return tr;
    }

    /** Get the current context in which we are translating. */
    public Context context() {
        return context;
    }

    /** Print an ast node using the given code writer.  This method should not
     * be called directly to translate a source file AST; use
     * <code>translate(Node)</code> instead.  This method should only be called
     * by nodes to print their children.
     */
    public void print(Node parent, Node child, CodeWriter w) {
        TypedTranslator tr;
        
        if (parent != null) {
            Context c = parent.del().enterChildScope(child, context);
            tr = this.context(c);
        }
        else {
            Context c = child.del().enterScope(context);
            tr = this.context(c);
        }
        
        child.del().translate(w, tr);
        
        if (parent != null) {
            parent.addDecls(context);
    	}
    }

	/**
	 * @param w
	 * @param source
	 * @param decl
	 */
	protected void translateTopLevelDecl(CodeWriter w, SourceFile source, TopLevelDecl decl) {
		Context c = source.del().enterScope(context);
		decl.del().translate(w, this.context(c));
	}
}
