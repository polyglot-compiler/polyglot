package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.io.IOException;

/**
 * A <code>FormalParameter</code> is immutable representation of a ordered 
 * pair: a type and a variable declarator identifier, used as formal
 * parameters such as in method declarations and catch blocks.
 */
public class FormalParameter extends Node 
{
  protected final TypeNode tn;
  protected final String name;
  protected final boolean isFinal;
  protected LocalInstance li;
    
  /**
   * Creates a new <code>FormalParameter</code>.
   */
  public FormalParameter( Node ext, TypeNode tn, String name, boolean isFinal) 
  {
    this.ext = ext;
    this.tn = tn;
    this.name = name;
    this.isFinal = isFinal;

    TypeSystem ts = tn.getType().getTypeSystem();
    AccessFlags modifiers = new AccessFlags();
    modifiers.setFinal( isFinal);
    this.li = ts.newLocalInstance( name, tn.getType(), modifiers );
  }

    public FormalParameter( TypeNode tn, String name, boolean isFinal) {
	this(null, tn, name, isFinal);
    }

  /**
   * Lazily reconstruct this node. 
   */
  public FormalParameter reconstruct( Node ext, TypeNode tn, String name, 
                                      boolean isFinal)
  {
    if( this.tn == tn && this.ext == ext && this.name.equals( name) && this.isFinal == isFinal) {
      return this;
    }
    else {
      FormalParameter n = new FormalParameter( ext, tn, name, isFinal);
      n.li = li;
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  public FormalParameter reconstruct( TypeNode tn, String name, 
                                      boolean isFinal) {
      return reconstruct(this.ext, tn, name, isFinal);
  }


  /**
   * Returns the type of this parameter.
   */
  public Type getParameterType() 
  {
    return tn.getType();
  }
  
  /**
   * Returns the name of this parameter.
   */
  public String getName() 
  {
    return name;
  }

  public LocalInstance getLocalInstance() {
    return li;
  }

  /**
   * Returns true if this parameter is final.
   */
  public boolean isFinal() 
  {
    return isFinal;
  }
  
  /**
   * Visit the children of this node.
   *
   * @pre Requires that <code>tn.visit</code> returns an object of type
   *  <code>TypeNode</code>.
   */
  public Node visitChildren(NodeVisitor v)
  {
    return reconstruct( Node.condVisit(this.ext, v), (TypeNode)tn.visit( v), name, isFinal);
  }

  public Node readSymbols( SymbolReader sr)
  {
    return null;
  }

  public Node cleanupSignatures( LocalContext c, SignatureCleaner sc) throws SemanticException, IOException
  {
    TypeNode newTN = (TypeNode) tn.visit(sc);

    AccessFlags modifiers = new AccessFlags();
    modifiers.setFinal( isFinal);

    LocalInstance oldLi = li;
    li = c.getTypeSystem().newLocalInstance( name, newTN.getType(), modifiers );
    li.copyAnnotationsFrom(oldLi);

    c.addSymbol( name, li );
    return reconstruct( Node.condVisit(this.ext, sc), newTN, name, isFinal);
  }

  public Node removeAmbiguities( LocalContext c) throws SemanticException
  {
    AccessFlags modifiers = new AccessFlags();
    modifiers.setFinal( isFinal);

    LocalInstance oldLi = li;
    li = c.getTypeSystem().newLocalInstance( name, tn.getType(), modifiers );
    li.copyAnnotationsFrom(oldLi);

    c.addSymbol( name, li );
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    if (c.isDefinedLocally( name) )
      throw new SemanticException("Duplicate declaration of \"" + name + "\"",
				  Annotate.getLineNumber(this));
    
    AccessFlags modifiers = new AccessFlags();
    modifiers.setFinal( isFinal);

    LocalInstance oldLi = li;
    li = c.getTypeSystem().newLocalInstance( name, tn.getType(), modifiers );
    li.copyAnnotationsFrom(oldLi);

    c.addSymbol( name, li );

    Annotate.setCheckedType( this, tn.getType());
    return this;
  }

  public void translate_no_override(LocalContext c, CodeWriter w)
  {
    tn.translate( c, w);
    w.write( " " + name);
  }

  public void dump( CodeWriter w)
  {
    w.write( "FORMAL PARAM <" + name + "> ");
    if( isFinal) {
      w.write( "< final > ");
    }
    dumpNodeInfo( w);
  }
}
