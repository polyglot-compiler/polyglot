package jltools.frontend;

import jltools.ast.*;
import jltools.parse.*;
import jltools.lex.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Main;

import java.io.*;
import java.util.*;

/**
 * A <code>SourceJob</code> encapsulates work done by the compiler on behalf
 * of a source file target.  It includes all information carried between
 * phases of the compiler.
 */
public class SourceJob extends Job
{
  Node ast;
  ErrorQueue eq;

  public SourceJob(Target t, ErrorQueue eq, Compiler c) {
    super(c, t);
    this.eq = eq;
  }

  public Node getAST() {
    return ast;
  }

  public void parse() {
    ExtensionInfo extInfo = compiler.getExtensionInfo();
    java_cup.runtime.Symbol sym = null;

    try {
      Reader reader = t.getSourceReader();
      java_cup.runtime.lr_parser grm = extInfo.getParser(reader, eq);
      sym = grm.parse();
    }
    catch (IOException e) {
      eq.enqueue( ErrorInfo.IO_ERROR, e.getMessage());
      return;
    }
    catch (SyntaxException e) {
      eq.enqueue( ErrorInfo.SYNTAX_ERROR, e.getMessage(), e.getLineNumber());
      return;
    }
    catch (Exception e) {
      e.printStackTrace();
      eq.enqueue( ErrorInfo.INTERNAL_ERROR, e.getMessage());
      return;
    }

    try {
      // done with the input
      t.closeSource();
    }
    catch (IOException e) {
      eq.enqueue( ErrorInfo.IO_ERROR, e.getMessage());
      return;
    }

    /* Figure out whether or not the parser was successful. */
    if (sym == null ||
        !(sym.value instanceof Node)) {
      eq.enqueue( ErrorInfo.SYNTAX_ERROR, "Unable to parse source file.");
      return;
    }

    ast = (Node) sym.value;

    runVisitors(PARSED);
  }

  public void read() {
    it = new ImportTable(compiler.getSystemResolver(), true, eq);
    runVisitors(READ);
    if (eq.hasErrors()) {
      return;
    }
    compiler.getParsedResolver().include(cr);
  }

  public void clean() {
    runVisitors(CLEANED);
  }
  public void disambiguate() {
    runVisitors(DISAMBIGUATED);
  }
  public void check() {
    runVisitors(CHECKED);
  }
  public void translate() {
    runVisitors(TRANSLATED);
    ast = null;
  }

  private void runVisitors(int stage) {
    NodeVisitor v;
    Node result = ast;

    ExtensionInfo extInfo = compiler.getExtensionInfo();

    for (Iterator iter = extInfo.getNodeVisitors(compiler, this, stage).iterator();
         iter.hasNext(); ) {

      v = (NodeVisitor) iter.next();

      Main.report(null, 2,
	"  Running visitor " + v.getClass().getName() + "...");

      result = result.visit( v);
      v.finish();

      if (eq.hasErrors()) {
	break;
      }
    }

    ast = result;

    if (eq.hasErrors()) {
      return;
    }

    status |= stage;
  }

  public void dump(CodeWriter w) throws IOException {
    if (ast != null) {
      DumpAst d = new DumpAst(w);
      ast.visit(d);
    }
    w.flush();
  }
}
