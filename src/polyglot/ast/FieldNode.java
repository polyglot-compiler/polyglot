/*
 * FieldNode.java
 */

package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.SymbolReader;

import java.util.*;

/**
 * FieldNode
 *
 * Overview: A FieldNode is a mutable representation of the
 * declaration of a field of a class.  It consists of a set of
 * AccessFlags and a VariableDeclarationStatement.
 */

public class FieldNode extends ClassMember {
  /**
   * Effects: Creates a new FieldNode declaring variables from
   * <declare> with modified by the flags in <accessFlags>.
   */
  public FieldNode (AccessFlags accessFlags,
		    VariableDeclarationStatement declare) {
    this.accessFlags = accessFlags;
    this.declare = declare;
  }

  /**
   * Effects: Returns the AccessFlags for these fields.
   */
  public AccessFlags getAccessFlags() {
    return accessFlags;
  }

  /**
   * Effects: Sets the access flags for these fields to be <newFlags>.
   */
  public void setAccessFlags(AccessFlags newFlags) {
    accessFlags = newFlags;
  }

  /**
   * Effects: Returns the VariableDeclarationStatement which specifies
   * the type, names, and initialization expressions for the fields
   * declared as part of this FieldNode.
   */
  public VariableDeclarationStatement getDeclare() {
    return declare;
  }

  /**
   * Effects: Sets the VariableDeclarationStatement for this FieldNode
   * to be <newDeclare>.
   */
  public void setDeclare(VariableDeclarationStatement newDeclare) {
    declare = newDeclare;
  }


  public void translate(LocalContext c, CodeWriter w)
  {
    //w.write(accessFlags.getStringRepresentation());
    declare.translate(c, w);
  }

  public Node dump( CodeWriter w)
  {
    w.write ( "( FIELD DECLARATION ");
       /* + accessFlags.getStringRepresentation() */
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  } 

  public Node readSymbols( SymbolReader sr) throws TypeCheckException
  {
    ParsedClassType clazz = sr.getCurrentClass();
    VariableDeclarationStatement.Declarator declarator;
    Iterator iter = declare.declarators();
    FieldInstance fi;

    while( iter.hasNext()) {
      declarator = (VariableDeclarationStatement.Declarator)iter.next();
      fi = new FieldInstance( declarator.name, 
                              declare.typeForDeclarator( declarator), 
                              clazz, declare.getModifiers());
      // if it is a constant numeric expression (final + initializer is IntLiteral)
      // mark it under FieldInstance
      if (declarator.initializer instanceof IntLiteral && 
          declarator.initializer != null &&
          declare.getModifiers().isFinal())
      {
        fi.setConstantValue ( new Long ( 
                ((IntLiteral)declarator.initializer).getLongValue()) );
      }

      Annotate.setLineNumber( fi, Annotate.getLineNumber( this));
      clazz.addField( fi);

    }
    return this;
  }

  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    // FIXME; implement
    return this;
  }

  Object visitChildren(NodeVisitor v) 
  {
    declare = (VariableDeclarationStatement) declare.visit(v);
    return v.mergeVisitorInfo( Annotate.getVisitorInfo( this),
                               Annotate.getVisitorInfo( declare));
  }

  public Node copy() {
    FieldNode fn = new FieldNode(accessFlags.copy(), declare);
    fn.copyAnnotationsFrom(this);
    return fn;
  }

  public Node deepCopy() {
    FieldNode fn =
      new FieldNode(accessFlags.copy(),
		    (VariableDeclarationStatement) declare.deepCopy());
    fn.copyAnnotationsFrom(this);
    return fn;
  }

  private AccessFlags accessFlags;
  private VariableDeclarationStatement declare;
}


