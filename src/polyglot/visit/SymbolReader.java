package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;

import java.io.IOException;


public class SymbolReader extends NodeVisitor
{
  private ClassResolver systemResolver;
  private TableClassResolver currentResolver;

  private Target target;
  private TargetFactory tf;

  private TypeSystem ts;
  private ErrorQueue eq;

  private ParsedClassType current;

  private String packageName;
  private ImportTable it;

  public SymbolReader( ClassResolver systemResolver, 
                       TableClassResolver currentResolver, 
                       Target target, TargetFactory tf,
                       TypeSystem ts, ErrorQueue eq)
  {
    this.systemResolver = systemResolver;
    this.currentResolver = currentResolver;

    this.target = target;
    this.tf = tf;

    this.ts = ts;
    this.eq = eq;
    current = null;
  }

  public Node override(Node n)
  {
    try
    {
      return n.readSymbols( this);
    }
    catch( SemanticException e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
                  Annotate.getLineNumber( n));
      return n;
    }
  }

  public ParsedClassType pushClass( String name)
  {
    String fullName;
    ParsedClassType newClass;

    newClass = new ParsedClassType( ts, current);

    if( current == null) {
      fullName = (packageName == null ? "" : 
                                packageName + ".") + name;
    }
    else {
      fullName = current.getFullName() + "." + name;
      current.addInnerClass( newClass);
    }

    newClass.setFullName( fullName);
    newClass.setShortName( name);

    currentResolver.addClass( fullName, newClass);

    current = newClass;
    return current;
  }

  public void popClass()
  {
    ClassType c = current.getContainingClass();

    if( !(c instanceof ParsedClassType || c == null)) {
      throw new InternalCompilerError( 
                  "Too many pops from containing class stack.");
    }
    current = (ParsedClassType)c;
  }

  public void setPackageName( String packageName) throws SemanticException
  {
    this.packageName = packageName;
    this.it = new ImportTable( systemResolver, true);

    it.addPackageImport("java.lang");
    if( packageName != null) {
      it.addPackageImport( packageName);
    }

    /* Now add the "root" of this source file's package tree to the 
     * source path. This will also throw an exception if the source
     * file is not located in an appropriate directory. */
    try
    {
      tf.addSourceDirectory( target, packageName);
    }
    catch( IOException e)
    {
      throw new SemanticException( "Expected to find \"" + target.getName()
                      + "\" in a directory matching the package name \"" 
                      + packageName + "\".");
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
