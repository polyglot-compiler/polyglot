package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.util.*;


/**
 * A <code>FieldNode</code> is an immutable representation of the declaration 
 * of a field of a class.  It consists of a set of <code>AccessFlags</code>
 * and a <code>VariableDeclarationStatement</code>.
 */

public class FieldNode extends ClassMember 
{
  protected final VariableDeclarationStatement declare;

  /**
   * Creates a new <code>FieldNode</code> declaring variables from
   * <code>declare</code> with modified by the flags in 
   * <code>accessFlags</code>.
   */
  public FieldNode( Node ext,
		    VariableDeclarationStatement declare) 
  {
    this.ext = ext;
    this.declare = declare;
  }

  public FieldNode( VariableDeclarationStatement declare) {
      this(null, declare);
  }


  /**
   * Lazily reconstruct this node.
   */
  public FieldNode reconstruct( Node ext,
                                VariableDeclarationStatement declare)
  {
    if( this.declare == declare && this.ext == ext) {
      return this;
    }
    else {
      FieldNode n = new FieldNode( ext, declare);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  public FieldNode reconstruct( VariableDeclarationStatement declare) 
    {
	return reconstruct(this.ext, declare);
    }


  /**
   * Returns the access flags for these fields.
   */
  public AccessFlags getAccessFlags() 
  {
    return declare.getAccessFlags();
  }

  /**
   * Returns the <code>VariableDeclarationStatement</code> which specifies
   * the type, names, and initialization expressions for the fields
   * declared as part of this <code>FieldNode</code>.
   */
  public VariableDeclarationStatement getDeclaration() 
  {
    return declare;
  }

  /**
   * Visit the children of this node.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v),
                        (VariableDeclarationStatement)declare.visit( v));
  }

  public Node readSymbols( SymbolReader sr) throws SemanticException
  {
    ParsedClassType clazz = sr.getCurrentClass();
    VariableDeclarationStatement.Declarator decl;
    Iterator iter = declare.declarators();

    AccessFlags af = getAccessFlags();
    if (clazz.getAccessFlags().isInterface()) {
      //field members of interfaces are implicitly
      //  public static final
      af.setPublic(true);
      af.setStatic(true);
      af.setFinal(true);
    }

    while( iter.hasNext()) {
      decl = (VariableDeclarationStatement.Declarator)iter.next();
      FieldInstance fi = sr.getTypeSystem().newFieldInstance( decl.name, 
                              declare.typeForDeclarator( decl), 
                              clazz, af);
      decl.setVariableInstance(fi);
      /* If it is a constant numeric expression (final + initializer is 
       * IntLiteral) then mark it "constant" under FieldInstance. */
      // FIXME other literal types?
      if( decl.initializer instanceof IntLiteral 
          && decl.initializer != null && getAccessFlags().isFinal()) {
        fi.setConstantValue( new Long(
            ((IntLiteral)decl.initializer).getValue())); 
      }

      Annotate.setLineNumber( fi, Annotate.getLineNumber( this));
      clazz.addField( fi);
    }
  
    visitChildren(sr);

    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    // FIXME; implement
    return this;
  }

  public void translate_no_override(LocalContext c, CodeWriter w)
  {
    //w.write(accessFlags.getStringRepresentation());
    declare.translate(c, w);
    w.newline( 0);
  }

  public void dump( CodeWriter w)
  {
    w.write ( "FIELD DECLARATION ");
       /* + accessFlags.getStringRepresentation() */
    dumpNodeInfo( w);
  } 
}


