package jltools.visit;


import jltools.ast.*;
import jltools.frontend.Compiler;
import jltools.types.*;
import jltools.util.*;


public class SymbolReader extends NodeVisitor
{
  private TypeSystem ts;
  private TableClassResolver cr;
  private ErrorQueue eq;

  private ParsedClassType current;

  private String packageName;
  private ImportTable it;

  public SymbolReader( TableClassResolver cr, TypeSystem ts, ErrorQueue eq)
  {
    this.ts = ts;
    this.cr = cr;
    this.eq = eq;
    current = null;
  }

  public Node visitBefore(Node n)
  {
    try
    {
      return n.readSymbols( this);
    }
    catch( TypeCheckException e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
                  Annotate.getLineNumber( n));
      return n;
    }
  }

  public void pushClass( String name)
  {
    current = new ParsedClassType( ts, current);

    current.setFullName( (packageName == null ? name : 
                                packageName + "." + name));
    cr.addClass( name, current);
  }

  public void popClass()
  {
    ClassType c = current.getContainingClass();
    if( !(c instanceof ParsedClassType)) {
      throw new InternalCompilerError( 
                  "Too many pops from containing class stack.");
    }
    current = (ParsedClassType)c;
  }

  public void setPackageName( String packageName) throws TypeCheckException
  {
    this.packageName = packageName;
    this.it = new ImportTable( Compiler.getSystemClassResolver());

    it.addPackageImport("java.lang");
    if( packageName != null) {
      it.addPackageImport( packageName);
    }
  }

  public ImportTable getImportTable()
  {
    return it;
  }

  public ParsedClassType getCurrentClass()
  {
    return current;
  }

  public TypeSystem getTypeSystem()
  {
    return ts;
  }
}
