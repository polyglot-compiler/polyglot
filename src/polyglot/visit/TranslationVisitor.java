package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;

import java.io.*;
import java.util.Collection;

/** A TranslationVisitor generates output code from the processed AST.
 */
public class TranslationVisitor extends NodeVisitor
{
  protected ExtensionFactory ef;
  protected ImportTable it;
  protected Target target;
  protected TypeSystem ts;
  protected ErrorQueue eq;
  protected int outputWidth;
  protected Collection outputFiles;

  /**
   * Create a TranslationVisitor. The output of the visitor is a collection
   * of files whose names are added to the collection <code>outputFiles</code>.
   */
  public TranslationVisitor(ExtensionFactory ef,
			    ImportTable it,
			    Target target,
			    TypeSystem ts,
			    ErrorQueue eq,
			    int outputWidth,
			    Collection outputFiles)
  {
    this.ef = ef;
    this.it = it;
    this.target = target;
    this.ts = ts;
    this.eq = eq;
    this.outputWidth = outputWidth;
    this.outputFiles = outputFiles;
  }

  public Node override(Node n)
  {
    if (n instanceof SourceFileNode) {
	SourceFileNode sfn = (SourceFileNode) n;

	try {
	    Writer ofw = target.getOutputWriter(sfn.getPackageName());
	    CodeWriter w = new CodeWriter(ofw, outputWidth);
	    LocalContext c = ts.getLocalContext(it, ef, this);
	    n.translate(c, w);
	    w.flush();
	    System.out.flush();
	    target.closeDestination();
	    outputFiles.addAll(target.outputFiles());
	}
	catch (IOException e) {
	  eq.enqueue(ErrorInfo.IO_ERROR,
		     "I/O error while translating: " + e.getMessage());
	}
    }

    // Never recurse: Node.translate() does that for us!
    return n;
  }
}
