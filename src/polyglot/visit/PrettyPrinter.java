package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.util.*;

import java.io.*;
import java.util.*;

/**
 * A PrettyPrinter generates output code from the processed AST.
 * Output is sent to a code writer passes into the printAst method.
 *
 * To use:
 *     new PrettyPrinter().printAst(node, new CodeWriter(out));
 */
public class PrettyPrinter
{
    protected boolean appendSemicolon = true;

    public PrettyPrinter() {
    }

    /** Flag indicating whether to print a ';' after certain statements.
     * This is used when pretty-printing for loops. */
    public boolean appendSemicolon() {
        return appendSemicolon;
    }

    /** Set a flag indicating whether to print a ';' after certain statements.
     * This is used when pretty-printing for loops. */
    public boolean appendSemicolon(boolean a) {
        boolean old = this.appendSemicolon;
        this.appendSemicolon = a;
	return old;
    }

    /** Print an AST node using the given code writer.  The
     * <code>CodeWriter.flush()</code> method must be called after this method
     * to ensure code is output.  Use <code>printAst</code> rather than this
     * method to print the entire AST; this method should only be called by
     * nodes to print their children.
     */
    public void print(Node parent, Node child, CodeWriter w) {
        if (child != null) {
            child.del().prettyPrint(w, this);
        }
    }

    /** Print an AST node using the given code writer.  The code writer
     * is flushed by this method. */
    public void printAst(Node ast, CodeWriter w) {
        print(null, ast, w);

        try {
            w.flush();
        }
        catch (IOException e) {
        }
    }
}
