/*
 * TypeNode.java
 */

package jltools.ast;

import jltools.types.Type;
import jltools.types.LocalContext;
import jltools.visit.SymbolReader;

import jltools.util.Annotate;
import jltools.util.CodeWriter;



/**
 * TypeNode
 *
 * Overview: An TypeNode represents the syntactic representation of a Type.
 *   We make this a separate node so that Visitors may treat types separately,
 *   and so that Field/Method accesses can treat targets uniformly.
 **/
public class TypeNode extends Node {

  public TypeNode(Type type) {
    this.type = type;
  }

  public TypeNode(TypeNode other) {
    this.type = other.type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  void visitChildren(NodeVisitor vis)
  {
    // nothing to do
  }
   
  public Node readSymbols( SymbolReader sr)
  {
    return this;
  } 
   
  public Node removeAmbiguities( LocalContext c)
  {
    type = c.checkAndResolveType( type);
    Annotate.setType( this, type);

    //Annotate.setType( this, c.checkAndResolveType( type));
    
    return this;
  }

  public Node typeCheck(LocalContext c)
  {
    return this;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write(type.getTypeString());
  }

   public Node dump( CodeWriter w)
   {
     w.write( "( TYPE");
     w.write( " < " + type.getTypeString() + "> ");
     dumpNodeInfo( w);
     w.write( ")");
     return null;
   }
 

  public Node copy() {
    TypeNode tn = new TypeNode(type);
    tn.copyAnnotationsFrom(this);
    return tn;
  }

  public Node deepCopy() {
    return copy();
  }

  private Type type;
}

