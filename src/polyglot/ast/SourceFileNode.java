package jltools.ast;

import jltools.frontend.Compiler;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.util.*;

/**
 * A <code>SourceFileNode</code> is an immutable representations of a Java
 * langauge source file.  It consists of a package name, a list of 
 * <code>ImportNode</code>s, and a list of <code>GlobalDeclaration</code>s.
 */
public class SourceFileNode extends Node 
{
  protected final String package_;
  protected final List imports;
  protected final List decls;

  // FIXME
  private ImportTable it;

  /**
   * Requires: <imports> contains elements only of type ImportNode,
   * <decls> contains elements only of type GlobalDeclaration.
   *
   * Effects: Creates a new SourceFileNode with filename <filename> in
   * package <package_>, containing imports in <imports> and
   * decls in <decls>.
   */
  public SourceFileNode( Node ext, String package_, List imports, List decls) 
  {
    this.ext = ext;
    this.package_ = package_;
    this.imports = TypedList.copyAndCheck( imports, ImportNode.class, true);
    this.decls = TypedList.copyAndCheck( decls, GlobalDeclaration.class, true);
  }

    public SourceFileNode( String package_, List imports, List decls) {
	this(null, package_, imports, decls);
    }

  public SourceFileNode reconstruct( Node ext, String package_, List imports, 
                                     List decls) 
  {
    if( package_ != null && ! !this.package_.equals( package_) || this.ext != ext ||
        package_ == null && this.package_ != null ||
        package_ != null && this.package_ == null
        || this.imports.size() != imports.size()
        || this.decls.size() != decls.size()) {
      SourceFileNode n = new SourceFileNode( ext, package_, imports, decls);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < imports.size(); i++) {
        if( this.imports.get( i) != imports.get( i)) {
          SourceFileNode n = new SourceFileNode( ext, package_, imports, decls);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }

      for( int i = 0; i < decls.size(); i++) {
        if( this.decls.get( i) != decls.get( i)) {
          SourceFileNode n = new SourceFileNode( ext, package_, imports, decls);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }


  public SourceFileNode reconstruct( String package_, List imports, 
                                     List decls) {
      return reconstruct(this.ext, package_, imports, decls);
  }

  /**
   * Returns the package name of this source file.
   */
  public String getPackageName() 
  {
    return package_;
  }

  /**
   * Returns the <code>ImportNode</code> at position <code>pos</code> in this
   * node.
   */
  public ImportNode getImportNodeAt( int pos) 
  {
    return (ImportNode)imports.get( pos);
  }

  /**
   * Returns an iterator which will return the <code>ImportNode<code>s of
   * this source file (in order).
   */
  public Iterator importNodes() 
  {
    return imports.iterator();
  }
  
  /**
   * Returns the <code>GlobalDeclaration</code> at position <code>pos</code> in this
   * node.
   */
  public GlobalDeclaration getGlobalDeclarationAt( int pos) 
  {
    return (GlobalDeclaration) decls.get( pos);
  }

  public Iterator declarations()
  {
    return decls.iterator();
  }

  // FIXME necessary?
  public ImportTable getImportTable()
  {
    return it;
  }

  public Node visitChildren( NodeVisitor v)
  {
    List newImports = new ArrayList( imports.size());
    List newDecls = new ArrayList( decls.size());

    for( Iterator iter = importNodes(); iter.hasNext(); ) {
      ImportNode in = (ImportNode)((ImportNode)iter.next()).visit( v);
      if( in != null) {
        newImports.add( in);
      }
    }

    for( Iterator iter = declarations(); iter.hasNext(); ) {
      GlobalDeclaration cn = (GlobalDeclaration)((Node)iter.next()).visit( v);
      if( cn != null) {
        newDecls.add( cn);
      }
    }

    return reconstruct( Node.condVisit(this.ext, v), package_, newImports, newDecls);
  }
  
  public Node readSymbols( SymbolReader sr) throws SemanticException
  {
    sr.setPackageName( package_);
    visitChildren(sr);
    return this;
  }

  public Node removeAmbiguities( LocalContext c)
  {
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Set vNames = new HashSet();
    Set publicDecls = new HashSet();

    /* FIXME
    String sSourceName = TypeSystem.getFirstComponent ( sourceFilename );
    */
     
    for (Iterator i = decls.iterator(); i.hasNext(); ) {
      GlobalDeclaration cn = (GlobalDeclaration) i.next();
      String s = cn.getName();

      if (vNames.contains(s)) {
	throw new SemanticException(
	  "The source file contains two global declarations named \"" +
	  s + "\".", Annotate.getLineNumber((Node) cn));
      }

      vNames.add(s);

      // FIXME: Is this right?
      if (cn.getAccessFlags().isPublic()) {
	if (! publicDecls.isEmpty()) {
	  throw new SemanticException(
	    "The source file contains more than one public global declaration.",
	    Annotate.getLineNumber((Node) cn));
	}

	publicDecls.add(cn);
      }

      /* FIXME
      if (cn.getAccessFlags().isPublic() && ! s.equals(sSourceName)) {
	throw new SemanticException("The name of the public declaration \"" +
	  s + "\" must match the source file name",
	  Annotate.getLineNumber((Node) cn));
      }
      */
    }
 
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    if( package_ != null && !package_.equals("")) {
      w.write( "package " + package_ + ";");
      w.newline(0);
      w.newline(0);
    }
    for( Iterator iter = importNodes(); iter.hasNext(); ) {
      ((ImportNode)iter.next()).translate( c, w);
    }
     
    if( !imports.isEmpty()) {
      w.newline(0);
    }

    for( Iterator iter = declarations(); iter.hasNext(); ) {
      ((Node) iter.next()).translate( c, w);
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "SOURCE FILE");
    //    w.write( " < " + sourceFilename + " >");
    w.write( " < " + package_ + " > ");
    dumpNodeInfo( w);
  }	
}
