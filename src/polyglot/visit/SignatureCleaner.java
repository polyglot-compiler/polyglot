package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import java.io.IOException;


/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself. 
 */
public class SignatureCleaner extends NodeVisitor
{
  protected ExtensionFactory ef;
  protected TypeSystem ts;
  protected ErrorQueue eq;
  protected LocalContext c;
  protected ImportTable it;
  protected TableClassResolver cr;
  protected ClassCleaner cc;

  public SignatureCleaner( ExtensionFactory ef,
			   TypeSystem ts, ImportTable it,
			   TableClassResolver cr,
			   ErrorQueue eq, ClassCleaner cc)
  {
    this.ef = ef;
    this.ts = ts;
    this.it = it;
    this.cr = cr;
    this.cc = cc;
    this.eq = eq;
    
    c = ts.getLocalContext( it, ef, this);
  }

  public boolean cleanClass(ClassType type) throws IOException
  {
    return cc.cleanClass(type);
  }

  public boolean containsClass(String name)
  {
    return cr.containsClass(name);
  }

  public boolean cleanPrerequisiteClass(ClassType clazz) throws IOException {
    if (clazz instanceof ParsedClassType) {
      if (! containsClass(clazz.getFullName())) {
	return cleanClass(clazz);
      } else {
	return true;
      }
    } else {
      return true;
    }
  }

  public Node override( Node n)
  {
    LocalContext.Mark mark = c.getMark();

    try {
      Node m;
      if (n.ext instanceof CleanupSignaturesOverride) {
        m = ((CleanupSignaturesOverride) n.ext).cleanupSignatures(n, this, c);
      }
      else {
	m = n.cleanupSignatures(c, this);
      }

      c.assertMark(mark);
      return m;
    }
    catch (SemanticException e) {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                  (e.getLineNumber() == SemanticException.INVALID_LINE ?
		   Annotate.getLineNumber( n) :
		   e.getLineNumber()) );
      c.popToMark(mark);
    }
    catch (IOException e) {
      eq.enqueue( ErrorInfo.IO_ERROR, e.getMessage(), 
                  Annotate.getLineNumber( n));
      c.popToMark(mark);
    }

    c.assertMark(mark);
    return null;
  }

  public NodeVisitor enter( Node n)
  {
    n.enterScope(c);
    return this;
  }

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    n.leaveScope(c);
    return n;
  }

  public ImportTable getImportTable()
  {
    return it;
  }
}
