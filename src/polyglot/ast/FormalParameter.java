/*
 * FormalParameter.java
 */

package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

/**
 * Overview: A FormalParameter is mutable representation of a pair of
 * values, a type and a variable declarator id, used as formal
 * parameters such as in method declarations and catch blocks.
 */

public class FormalParameter extends Node {
    
    /**
     * Effects: Creates a new FormalParameter of type <type> bound to
     * the name <name>.  The FormalParameter is final if <isFinal> is
     * true.
     */
    public FormalParameter (TypeNode type, String name, boolean isFinal) {
	this.type = type;
	this.name = name;
	this.isFinal = isFinal;
    }

    /**
     * Effects: Creates a new FormalParameter of type <type> bound to
     * the name <name>.  The FormalParameter is final if <isFinal> is
     * true.
     */
    public FormalParameter (Type type, String name, boolean isFinal) {
	this.type = new TypeNode(type);
	this.name = name;
	this.isFinal = isFinal;
    }

    /**
     * Effects: Returns the type of this parameter.
     */
    public Type getType() {
	return type.getType();
    }

  public TypeNode getTypeNode() {
    return type;
  }

    /**
     * Effects: Sets the type of this FormalParameter to be <newType>.
     */
    public void setType(TypeNode newType) {
	type = newType;
    }

    /**
     * Effects: Sets the type of this FormalParameter to be <newType>.
     */
    public void setType(Type newType) {
	type = new TypeNode(newType);
    }

    /**
     * Effects: Returns the name of this FormalParameter.
     */
    public String getName() {
	return name;
    }

    /**
     * Effects: Sets the name of this FormalParameter to be newName.
     */
    public void setName(String newName) {
	name = newName;
    }

    /**
     * Effects: returns true if this FormalParameter is final.
     */
    public boolean isFinal() {
	return isFinal;
    }

    /**
     * Effects: if <isFinal> is true, sets this FormalParameter to be
     * final.  If <isFinal> is false, sets this FormalParameter to not
     * be final.
     */
    public void setFinal(boolean isFinal) {
	this.isFinal = isFinal;
    }

  public Node readSymbols( SymbolReader sr)
  {
    return this;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    type.translate( c, w);
    w.write ( " " + name);
  }

  public Node dump( CodeWriter w)
  {
    w.write( "( FORMAL PARAM <" + name + "> ");
    if( isFinal) {
      w.write( "< final > ");
    }
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node removeAmbiguities( LocalContext c ) throws TypeCheckException
  {
    AccessFlags modifiers = new AccessFlags ( );
    modifiers.setFinal ( isFinal );
    c.addSymbol( name, new FieldInstance ( name, type.getType(), null, modifiers ));
    return this;
  }

  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    AccessFlags modifiers = new AccessFlags ( );
    modifiers.setFinal ( isFinal );
    c.addSymbol( name, new FieldInstance( name, type.getType(), null, modifiers ));

    Annotate.setCheckedType( this, type.getType());
    return this;
  }
  
  Object visitChildren(NodeVisitor v)
  {
    type.visit(v);
    return v.mergeVisitorInfo( Annotate.getVisitorInfo( this),
                               Annotate.getVisitorInfo( type));
  }
  
  public Node copy()
  {
    return new FormalParameter((TypeNode)type.copy(), 
                               name, 
                               isFinal);
  }
  public Node deepCopy() {
    return new FormalParameter((TypeNode) type.deepCopy(),
	                          	 name,
	                      			 isFinal);
  }

  private TypeNode type;
  private String name;
  private boolean isFinal;
}
