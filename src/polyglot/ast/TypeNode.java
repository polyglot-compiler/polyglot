package jltools.ast;

import jltools.frontend.Compiler;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;


/**
 * A <code>TypeNode</code> represents the syntactic representation of a 
 * <code>Type</code> within the abstract syntax tree.
 */
public class TypeNode extends Node 
{

  /**
   * The <code>Type</code> encapsulated by this node.
   */
  protected final Type type;

  /**
   * The string that originally represented the type in the source file.
   * @see jltools.ast.TypeNode#translate
   */
  protected final String original;

  public TypeNode( Node ext, Type type) 
  { 
    this( ext, type, type.getTypeString());
  }

    public TypeNode( Type type) {
	this(null, type);
    }

  public TypeNode( Node ext, Type type, String original) 
  {
    this.ext = ext;
    this.type = type;
    this.original = original;
  }

    public TypeNode( Type type, String original) {
	this(null, type, original);
    }

  /**
   * Lazily reconstruct this node.
   * <p>
   * If the arguments are pointer identical the fields of the current node,
   * then the current node is returned untouched. Otherwise a new node is
   * constructed with the new fields and all annotations from this node are
   * copied over.
   *
   * @param type The new type of this node.
   * @param original The original string that represents .
   * @return An <code>ArrayIndexExpression<code> with the given base and index.
   */
  public TypeNode reconstruct( Node ext, Type type, String original) 
  {
    if( this.type == type && this.ext == ext && this.original.equals( original)) {
      return this;
    }
    else {
      TypeNode n = new TypeNode( ext, type, original);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public TypeNode reconstruct( Type type, String original) {
	return reconstruct(this.ext, type, original);
    }

  public Type getType() 
  {
    return type;
  }

    public String getOriginal() {
	return original;
    }

  public Node visitChildren(NodeVisitor v)
  {
    /* Nothing to do. */
      return reconstruct(Node.condVisit(ext, v), type, original);
  }
   
  public Node readSymbols( SymbolReader sr)
  {
    return this;
  } 
   
  public Node removeAmbiguities( LocalContext c) throws SemanticException
  {
    return reconstruct( ext, c.getType( type), original);
  }

  public Node typeCheck(LocalContext c)
  {
    setCheckedType( type);
    return this;
  }

  /*
   * If the "use-fully-qualified-class-names" options is used, then the fully
   * qualified names is written out (<code>java.lang.Object</code>). Otherwise,
   * the string that originally represented the type in the source file is 
   * used.
   */
  public void translate(LocalContext c, CodeWriter w)
  {
    if( Compiler.useFullyQualifiedNames()) {
      w.write( type.getTypeString());
    }
    else {
      w.write( original);
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "TYPE");
    w.write( " < " + type.getTypeString() + " > ");
    if( type instanceof AmbiguousType) {
      w.write( "AMB ");
    }
    //dumpNodeInfo( w);
  }

  public void setCheckedType( Type type)
  {
    Annotate.setCheckedType( this, type);
  }

  public Type getCheckedType()
  {
    return Annotate.getCheckedType( this);
  }
}

