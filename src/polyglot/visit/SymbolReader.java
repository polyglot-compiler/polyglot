package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;

import java.io.IOException;
import java.util.*;


public class SymbolReader extends NodeVisitor
{
  protected TableClassResolver currentResolver;

  protected TypeSystem ts;
  protected ErrorQueue eq;

  protected ParsedClassType current;

  protected String packageName;
  protected ImportTable it;

  public SymbolReader( ImportTable it,
                       TableClassResolver currentResolver, 
                       TypeSystem ts, ErrorQueue eq)
  {
    this.it = it;
    this.currentResolver = currentResolver;

    this.ts = ts;
    this.eq = eq;
    current = null;
  }

  public Node override(Node n)
  {
    try {
      if (n.ext instanceof ReadSymbolsOverride) {
	return ((ReadSymbolsOverride) n.ext).readSymbols(n, this);
      }
      else {
	return n.readSymbols( this);
      }
    }
    catch( SemanticException e)
    {
      eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
		  Annotate.getPosition( n));
      return n;
    }
  }

  public ParsedClassType pushClass( String name,
				    boolean isLocal, boolean isAnonymous)
  {
    String fullName;
    ParsedClassType newClass;

    newClass = ts.newParsedClassType(it, current);

    if( current == null) {
      if (isLocal || isAnonymous) {
	throw new InternalCompilerError(	
	    "Top-level class cannot be local or anonymous");
      }
      fullName = (packageName == null ? "" : 
                                packageName + ".") + name;
    }
    else {
      if (isLocal || isAnonymous) {
	String prefix = isLocal ? "$Local$" : "$Anonymous$";
	int i = 1;
	String suffix;
	do {
	  suffix = prefix + i + "." + name;
	  i++;
	} while (current.getInnerNamed(suffix) != null);
	fullName = current.getFullName() + "." + suffix;
      }
      else {
	fullName = current.getFullName() + "." + name;
      }
      current.addInnerClass( newClass);
    }

    newClass.setFullName( fullName);
    newClass.setShortName( name);
    if (packageName != null)
      newClass.setPackageName( packageName);

    newClass.setInner(current != null);
    newClass.setIsLocal(isLocal);
    newClass.setIsAnonymous(isAnonymous);

    if (current == null && ! isLocal && ! isAnonymous) {
	currentResolver.addClass( fullName, newClass);
    }

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

  public String getPackageName() {
    return packageName;
  }

  protected void addDefaultPackageImports() throws SemanticException {
    for (Iterator i = ts.defaultPackageImports().iterator(); i.hasNext(); ) {
	String pkg = (String) i.next();
	it.addPackageImport(pkg);
    }
  }

  public void setPackageName( String packageName) throws SemanticException
  {
    this.packageName = packageName;

    addDefaultPackageImports();

    if( packageName != null) {
      it.addPackageImport( packageName);
    }
  }

  public TableClassResolver getCurrentResolver() {
    return currentResolver;
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
