package jltools.visit;


import jltools.ast.Node;
import jltools.ast.NodeVisitor;
import jltools.types.*;


public class SymbolReader extends NodeVisitor
{
  private TypeSystem ts;
  private TableClassResolver cr;
  private ParsedJavaClass current;

  public SymbolReader( TypeSystem ts, TableClassResolver cr)
  {
    this.ts = ts;
    this.cr = cr;
    current = new ParsedJavaClass( ts);
  }

  public Node visitBefore(Node n)
  {
    n.readSymbols( this);
    return null;
  }
}
