/*
 * ImportNode.java
 */

package jltools.ast;


import jltools.types.*;
import jltools.util.CodeWriter;
import jltools.visit.SymbolReader;

/**
 * Overview: An ImportNode is a mutable representation of a Java
 * import statement.  It consists of the string representing the item
 * being imported and a type which is either indicating that a class
 * is being imported, or that an entire package is being imported.
 */

public class ImportNode extends Node {
  /** Indicates that a single class is being imported. */
  public static final int CLASS = 0;
  /** Indicates that an entire package is being imported. */
  public static final int PACKAGE = 1;
  public static final int MAX_TYPE = PACKAGE;

  /**
   * Requires: <type> is a valid type as defined by the public static
   * ints defined in this class.
   * 
   * Overview: Creates a new ImportNode which is of type <type> (where
   * the values for type are defined by the public static ints of this
   * class) and imports <imports>.
   */
  public ImportNode(int type, String imports) {
    setType(type);
    this.imports = imports;
  }

  /**
   * Effects: Returns the type of this ImportNode.
   */
  public int getType() {
    return type;
  }

  /**
   * Requires: <newType> is a valid type for an ImportNode, as defined
   * by the public static ints of this class.
   *
   * Effects: Sets the type of this ImportNode to <newType>.
   */
  public void setType(int newType) {
    if (type < 0 || type > MAX_TYPE) {
      throw new IllegalArgumentException ("Invalid type for ImportNode.");
    }
    type = newType;
  }

  /**
   * Effects: Returns the string indicating the item being imported.
   */
  public String getImports() {
    return imports;
  }

  /**
   * Effects: Sets the string indicating the item being imported to
   * <newImports>.
   */
  public void setImports(String newImports) {
    imports = newImports;
  }


  public void translate(LocalContext c, CodeWriter w)
  {
    w.write("import " + imports+ (type == PACKAGE ? ".*;" : ";" ));
    w.newline(0);
  }

  public Node dump( CodeWriter w)
  {
    w.write( "( IMPORT < " + (type == PACKAGE ? "PACKAGE" : "CLASS"));
    w.write( " > < " + imports + " > ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node readSymbols( SymbolReader sr) throws TypeCheckException
  {
    ImportTable it = sr.getImportTable();

    System.err.println( "Adding import: " + imports);
    switch( type)
    {
    case CLASS:
      it.addClassImport( imports);
      break;
    case PACKAGE:
      it.addPackageImport( imports);
      break;
    }
    return this;
  }
   
  public Node typeCheck(LocalContext c)
  {
    // FIXME; implement
    return this;
  }

  public void visitChildren(NodeVisitor v) {
  }

  public Node copy() {
    ImportNode in = new ImportNode (type, imports);
    in.copyAnnotationsFrom(this);
    return in;
  }

  public Node deepCopy() {
    return copy();
  }

  private int type;
  private String imports;
}

