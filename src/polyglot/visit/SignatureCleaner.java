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
  protected TypeSystem ts;
  protected ErrorQueue eq;
  protected LocalContext c;
  protected ImportTable it;
  protected TableClassResolver cr;
  protected ClassCleaner cc;

  public SignatureCleaner( TypeSystem ts, ImportTable it, TableClassResolver cr,
			   ErrorQueue eq, ClassCleaner cc)
  {
    this.ts = ts;
    this.it = it;
    this.cr = cr;
    this.cc = cc;
    this.eq = eq;
    
    c = ts.getLocalContext( it, this);
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
    try
    {
      return n.cleanupSignatures(c, this);
    }
    catch( SemanticException e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                  (e.getLineNumber() == SemanticException.INVALID_LINE ?
		   Annotate.getLineNumber( n) :
		   e.getLineNumber()) );
      // FIXME n.setHasError( true);
    }
    catch( IOException e)
    {
      eq.enqueue( ErrorInfo.IO_ERROR, e.getMessage(), 
                  Annotate.getLineNumber( n));
      // FIXME n.setHasError( true);
    }

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
