package jltools.frontend;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.types.Package;
import jltools.frontend.Compiler;

import java.io.*;
import java.util.*;

/** An output pass generates output code from the processed AST. */
public class PrettyPrintPass extends AbstractPass
{
    protected Job job;
    protected PrettyPrinter pp;
    protected CodeWriter w;

    /**
     * Create a PrettyPrinter.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public PrettyPrintPass(Pass.ID id, Job job, CodeWriter w, PrettyPrinter pp) {
	super(id);
        this.job = job;
        this.pp = pp;
        this.w = w;
    }

    public boolean run() {
        Node ast = job.ast();

        if (ast == null) {
            w.write("<<<< null AST >>>>");
        }
        else {
            pp.printAst(ast, w);
        }

        return true;
    }
}
