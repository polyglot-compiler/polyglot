/*
 * SourceFileNode.java
 */

package jltools.ast;

import java.util.*;

import jltools.frontend.Compiler;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.SymbolReader;

/**
 * Overview: A SourceFileNode is a mutable representations of a Java
 * langauge source file.  It consists of a filename, a package name, a
 * list of ImportNodes, and a list of ClassNodes.
 */

public class SourceFileNode extends Node {
  /**
   * Requires: <imports> contains elements only of type ImportNode,
   * <classes> contains elemetns only of type ClassNode.
   *
   * Effects: Creates a new SourceFileNode with filename <filename> in
   * package <packageName>, containing imports in <imports> and
   * classes in <classes>.
   */
  public SourceFileNode(String filename, String packageName,
			List imports, List classes) {
    this.sourceFilename = filename;
    this.packageName = packageName;
    TypedList.check(imports, ImportNode.class);
    this.imports = new ArrayList(imports);
    TypedList.check(classes, ClassNode.class);
    this.classes = new ArrayList(classes);
  }

  /**
   * Effects: Returns the filename represented by this SourceFileNode.
   */
  public String getFilename() {
    return sourceFilename;
  }
  
  /**
   * Effects: Sets the filename represented by this SourceFileNode to
   * <newFilename>.
   */
  public void setFilename(String newFilename) {
    sourceFilename = newFilename;
  }

  /**
   * Effects: Returns the package name of this.
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Effects: Sets the package name of this SourceFileNode to be
   * <newPackageName>.
   */
  public void setPackageName(String newPackageName) {
    packageName = newPackageName;
  }

  /**
   * Effects: Adds ImportNode <in> to the imports of this SourceFileNode.
   */
  public void addImportNode(ImportNode in) {
    imports.add(in);
  }

  /**
   * Effects: Returns the ImportNode at position <pos> in this
   * SourceFileNode.  Throws an IndexOutOfBoundsException if <pos> is
   * not valid.  
   */
  public ImportNode getImportNode(int pos) {
    return (ImportNode) imports.get(pos);
  }

  /**
   * Effects: Removes the ImportNode at position <pos> in this
   * SouceFileNode.  Throws an IndexOutOfBoundsExeption if <pos> is
   * not valid.
   */
  public void removeImportNode(int pos) {
    imports.remove(pos);
  }

  /**
   * Effects: Returns a TypedListIterator which will return the
   * ImportNodes of this SourceFileNode in order.
   */
  public TypedListIterator importNodes() {
    return new TypedListIterator(imports.listIterator(),
				 ImportNode.class,
				 false);
  }

  /**
   * Effects: Adds a ClassNode to the list of classes contained in
   * this SourcEfileNode.
   */
  public void addClassNode(ClassNode cn) {
    classes.add(cn);
  }
  
  /**
   * Effects: Returns the ClassNode at position <pos> in this
   * SourceFileNode.  Throws an IndexOutOfBoundsException if <pos> is
   * not valid.
   */
  public ClassNode getClassNode(int pos) {
    return (ClassNode) classes.get(pos);
  }

  /**
   * Effects: Removes the ClassNode at position <pos> in this
   * SourceFileNode. Throws an IndexOutOfBoundsException if <pos> is
   * not valid.
   */
  public void removeClassNode(int pos) {
    classes.remove(pos);
  }

  public ImportTable getImportTable()
  {
    return it;
  }


   void visitChildren(NodeVisitor vis)
   {
      for(ListIterator it=imports.listIterator(); it.hasNext(); ) {
	 it.set(((ImportNode) it.next()).visit(vis));
      }
      for(ListIterator it=classes.listIterator(); it.hasNext(); ) {
	 it.set(((ClassNode) it.next()).visit(vis));
      }
   }
   
   public Node readSymbols( SymbolReader sr) throws TypeCheckException
   {
     sr.setPackageName( packageName);
     return null;
   }

  public Node removeAmbiguities( LocalContext c)
  {
    return this;
  }

   public Node typeCheck(LocalContext c)
   {
      return this;
   }

   public void translate(LocalContext c, CodeWriter w)
   {
     if (packageName != null && !packageName.equals(""))
     {
       w.write("package " + packageName + ";");
       w.newline(0);
       w.newline(0);
     }
     for(ListIterator it=imports.listIterator(); it.hasNext(); ) 
     {
       ((ImportNode)it.next()).translate(c, w);
     }
     if (!imports.isEmpty())
       w.newline(0);
     for(ListIterator it=classes.listIterator(); it.hasNext(); ) 
     {
       ((ClassNode)it.next()).translate(c, w);
     }
   }

   public Node dump( CodeWriter w)
   {
     w.write( "( SOURCE FILE");
     w.write( " < " + sourceFilename + " >");
     w.write( " < " + packageName + " > ");
     dumpNodeInfo( w);
     w.write( ")");
     return null;
   }

  public Node copy() {
    SourceFileNode sf = new SourceFileNode(sourceFilename, packageName,
					   imports, classes);
    sf.copyAnnotationsFrom(this);
    return sf;
  }
			
  public Node deepCopy() {
    List newImports = new ArrayList(imports.size());
    for(ListIterator it=imports.listIterator(); it.hasNext(); ) {
      newImports.add(((ImportNode) it.next()).deepCopy());
    }
    List newClasses = new ArrayList(classes.size());
    for(ListIterator it=classes.listIterator(); it.hasNext(); ) {
      newClasses.add(((ClassNode) it.next()).deepCopy());
    }
    SourceFileNode sf = new SourceFileNode(sourceFilename, packageName,
					   newImports, newClasses);
    sf.copyAnnotationsFrom(this);
    return sf;
  }

  private String sourceFilename;
  private String packageName;
  private List imports;
  private List classes;

  private ImportTable it;
}
