package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.util.*;

import java.io.*;
import java.util.*;

/**
 * A PrettyPrinter generates output code from the processed AST.
 *
 * To use:
 *     new PrettyPrinter().printAst(node, new CodeWriter(out));
 */
public class PrettyPrinter
{
    protected boolean appendSemicolon = true;

    public PrettyPrinter() {
    }

    public boolean appendSemicolon() {
        return appendSemicolon;
    }

    public boolean appendSemicolon(boolean a) {
        boolean old = this.appendSemicolon;
        this.appendSemicolon = a;
	return old;
    }

    public void print(Node ast, CodeWriter w) {
        if (ast != null) {
            ast.del().prettyPrint(w, this);
        }
    }

    public void printAst(Node ast, CodeWriter w) {
        print(ast, w);

        try {
            w.flush();
        }
        catch (IOException e) {
        }
    }
}
