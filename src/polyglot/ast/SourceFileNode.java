package jltools.ast;

import jltools.frontend.Compiler;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.util.*;
import splitter.util.*;
import jltools.ext.jif.ast.*;

/**
 * A <code>SourceFileNode</code> is an immutable representations of a Java
 * langauge source file.  It consists of a package name, a list of 
 * <code>ImportNode</code>s, and a list of <code>ClassNode</code>s.
 */
public class SourceFileNode extends Node 
{
  protected final String package_;
  protected final List imports;
  protected final List classes;

  // FIXME
  private ImportTable it;

  /**
   * Requires: <imports> contains elements only of type ImportNode,
   * <classes> contains elemetns only of type ClassNode.
   *
   * Effects: Creates a new SourceFileNode with filename <filename> in
   * package <package_>, containing imports in <imports> and
   * classes in <classes>.
   */
  public SourceFileNode( Node ext, String package_, List imports, List classes) 
  {
    this.ext = ext;
    this.package_ = package_;
    this.imports = TypedList.copyAndCheck( imports, ImportNode.class, true);
    this.classes = TypedList.copyAndCheck( classes, ClassNode.class, true);
  }

    public SourceFileNode( String package_, List imports, List classes) {
	this(null, package_, imports, classes);
    }

  public SourceFileNode reconstruct( Node ext, String package_, List imports, 
                                     List classes) 
  {
    if( package_ != null && ! !this.package_.equals( package_) || this.ext != ext ||
        package_ == null && this.package_ != null ||
        package_ != null && this.package_ == null
        || this.imports.size() != imports.size()
        || this.classes.size() != classes.size()) {
      SourceFileNode n = new SourceFileNode( ext, package_, imports, classes);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < imports.size(); i++) {
        if( this.imports.get( i) != imports.get( i)) {
          SourceFileNode n = new SourceFileNode( ext, package_, imports, classes);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }

      for( int i = 0; i < classes.size(); i++) {
        if( this.classes.get( i) != classes.get( i)) {
          SourceFileNode n = new SourceFileNode( ext, package_, imports, classes);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }


  public SourceFileNode reconstruct( String package_, List imports, 
                                     List classes) {
      return reconstruct(this.ext, package_, imports, classes);
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
   * Returns the <code>ClassNode</code> at position <code>pos</code> in this
   * node.
   */
  public ClassNode getClassNodeAt( int pos) 
  {
    return (ClassNode)classes.get( pos);
  }

  public Iterator classNodes()
  {
    return classes.iterator();
  }

  // FIXME necessary?
  public ImportTable getImportTable()
  {
    return it;
  }

  public Node visitChildren( NodeVisitor v)
  {
    List newImports = new ArrayList( imports.size()),
      newClasses = new ArrayList( classes.size());

    for( Iterator iter = importNodes(); iter.hasNext(); ) {
      ImportNode in = (ImportNode)((ImportNode)iter.next()).visit( v);
      if( in != null) {
        newImports.add( in);
      }
    }

    for( Iterator iter = classNodes(); iter.hasNext(); ) {
      ClassNode cn = (ClassNode)((ClassNode)iter.next()).visit( v);
      if( cn != null) {
        newClasses.add( cn);
      }
    }

    return reconstruct( Node.condVisit(this.ext, v), package_, newImports, newClasses);
  }
  
  public Node readSymbols( SymbolReader sr) throws SemanticException
  {
    sr.setPackageName( package_);
    return null;
  }

  public Node removeAmbiguities( LocalContext c)
  {
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    /* FIXME
    Vector vNames = new Vector();
    String sSourceName = TypeSystem.getFirstComponent ( sourceFilename );
     
     for(ListIterator it=classes.listIterator(); it.hasNext(); ) {
       ClassNode cn = (ClassNode)it.next();
       String s = TypeSystem.getShortNameComponent (cn.getName() );
       if ( vNames.contains (s ) )
         throw new SemanticException ( "The source file contains two classes named \"" + s + "\".", 
                                        Annotate.getLineNumber ( cn ));
       vNames.add ( s );
       if ( cn.getAccessFlags().isPublic() && !s.equals ( sSourceName ) )
         throw new SemanticException ( "The name of the public class \"" + s + 
                                        "\" must match the source file name", 
                                        Annotate.getLineNumber( cn ));
     }
    */
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

    for( Iterator iter = classNodes(); iter.hasNext(); ) {
      ((ClassNode)iter.next()).translate( c, w);
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "SOURCE FILE");
    //    w.write( " < " + sourceFilename + " >");
    w.write( " < " + package_ + " > ");
    dumpNodeInfo( w);
  }	
  
  public void translate(CodeGenerator cg) {
	  //write interface
	  for( Iterator iter = importNodes(); iter.hasNext(); ) {
		  ImportNode importNode = (ImportNode) iter.next();
		  importNode.translate( null , cg.ri);
		  importNode.translate( null , cg.master);
		  for (Iterator it = cg.slaveWriters(); it.hasNext(); ) {
			  CodeWriter slave = (CodeWriter) it.next();
			  importNode.translate( null , slave);
		  }
      }
 	  
	  //IGNORE: multi classes in one souce file
	  ClassNode cn = (ClassNode) classes.get(0);
	  //((JifClassNode)cn).translate(cg);
  }
}
