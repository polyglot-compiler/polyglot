package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;


/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself. 
 */
public class AmbiguityRemover extends NodeVisitor
{
  protected TypeSystem ts;
  protected ErrorQueue eq;
  protected LocalContext c;
  protected ImportTable it;

  public AmbiguityRemover( ExtensionFactory ef,
    TypeSystem ts, ImportTable it, ErrorQueue eq)
  {
    this.ts = ts;
    this.it = it;
    this.eq = eq;
    
    c = ts.getLocalContext( it, ef, this);
  }

  public NodeVisitor enter( Node n)
  {
    n.enterScope( c);
    return this;
  }

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    try
    {
      Node m = n.removeAmbiguities( c);

      m.leaveScope( c);
      return m;
    }
    catch( SemanticException e)
    {
      int line = e.getLineNumber();

      if( line == SemanticException.INVALID_LINE) {
	  line = Annotate.getLineNumber( n);
      }

      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), line);

      n.leaveScope( c);
      return n;
    }
  }

  public Node override( Node n)
  {
    LocalContext.Mark mark = c.getMark();

    try {
      if (n.ext instanceof RemoveAmbiguitiesOverride) {
	return ((RemoveAmbiguitiesOverride) n.ext).removeAmbiguities(
		n, this, c);
      }
      else if (n instanceof VariableDeclarationStatement) {
        Node m = ((VariableDeclarationStatement)n).removeAmbiguities(c, this);
	c.assertMark(mark);
        return m;
      }
    }
    catch (SemanticException e) {
      eq.enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
		 Annotate.getLineNumber(n));
      c.popToMark(mark);
      return n;
    }

    return null;
  }

  public ImportTable getImportTable()
  {
    return it;
  }
}
