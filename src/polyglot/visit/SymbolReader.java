package jltools.visit;


import jltools.ast.*;
import jltools.types.*;


public class SymbolReader extends NodeVisitor
{
  private TypeSystem ts;
  private TableClassResolver cr;
  private ParsedJavaClass current;

  private String packageName;
  private ImportTable it;

  public SymbolReader( TypeSystem ts, TableClassResolver cr)
  {
    this.ts = ts;
    this.cr = cr;
    current = new ParsedJavaClass( ts);
  }

  public Node visitBefore(Node n)
  {
    if( n instanceof SourceFileNode)
    {
      SourceFileNode sfn = (SourceFileNode)n;
      packageName = sfn.getPackageName();
    }
    else if( n instanceof ClassNode)
    {
      

    }
    n.readSymbols( this);
    return null;
  }

  public Node visitAfter( Node n)
  {
    if( n instanceof ClassNode)
    {


    }
    return n;
  }

  public ParsedJavaClass getCurrentClass()
  {
    return current;
  }

  public void setImportTable( ImportTable table)
  {
    it = table;
  }

  public ImportTable getImportTable()
  {
    return it;
  }

  public String getPackageName()
  {
    return packageName;
  }
}
