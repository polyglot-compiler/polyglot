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

  public AmbiguityRemover( TypeSystem ts, ImportTable it, ErrorQueue eq)
  {
    this.ts = ts;
    this.it = it;
    this.eq = eq;
    
    c = ts.getLocalContext( it, this);
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
System.out.println("removing ambiguities from " + n.getClass().getName());
      Node m = n.removeAmbiguities( c);
System.out.println("removed ambiguities from "
	+ n.getClass().getName() + " now "
	+ m.getClass().getName());

      m.leaveScope( c);
      return m;
    }
    catch( SemanticException e)
    {
e.printStackTrace();
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
    if ( n instanceof VariableDeclarationStatement)
    {
      try
      {
        Node m = ((VariableDeclarationStatement)n).removeAmbiguities(c, this);
        return m;
      }
      catch ( SemanticException e)
      {
        eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                    Annotate.getLineNumber(n ));
        // FIXME: n.setHasError(true);
        return n;
      }
    }
    return null;
  }

  public ImportTable getImportTable()
  {
    return it;
  }
}
