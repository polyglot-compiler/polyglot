package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Pass;
import jltools.frontend.Job;
import java.util.LinkedList;

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

  public SignatureCleaner( Pass pass, ExtensionFactory ef,
	  TypeSystem ts, ImportTable it, ErrorQueue eq)
  {
    this.ef = ef;
    this.ts = ts;
    this.it = it;
    this.eq = eq;
    
    c = ts.getLocalContext( it, ef, pass);
  }

  public Node override(Node n) {
    if (n instanceof VariableDeclarationStatement || n instanceof ClassNode) {
      try {
	Node m;

	if (n.ext instanceof CleanupSignaturesOverride) {
	  m = ((CleanupSignaturesOverride) n.ext).cleanupSignatures(n, this, c);
	}
	else {
	  m = n.cleanupSignatures(c, this);
	}

	return m;
      }
      catch (SemanticException e) {
	eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
		    (e.getPosition() == null ?
		     Annotate.getPosition(n) :
		     e.getPosition()) );
	return n;
      }
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
    try {
      Node m;

      if (n.ext instanceof CleanupSignaturesOverride) {
        m = ((CleanupSignaturesOverride) n.ext).cleanupSignatures(n, this, c);
      }
      else {
	m = n.cleanupSignatures(c, this);
      }

      m.leaveScope(c);

      return m;
    }
    catch (SemanticException e) {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                  (e.getPosition() == null ?
		   Annotate.getPosition(n) :
		   e.getPosition()) );
    }

    n.leaveScope(c);

    return n;
  }

  public ImportTable getImportTable()
  {
    return it;
  }
}
