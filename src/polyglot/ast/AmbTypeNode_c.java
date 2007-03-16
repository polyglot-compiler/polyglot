/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * An <code>AmbTypeNode</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a type.
 */
public class AmbTypeNode_c extends TypeNode_c implements AmbTypeNode {
  protected QualifierNode qual;
  protected Id name;

  public AmbTypeNode_c(Position pos, QualifierNode qual,
                       Id name) {
    super(pos);
assert(name != null); // qual may be null
    this.qual = qual;
    this.name = name;
  }

  public Id id() {
      return this.name;
  }
  
  public AmbTypeNode id(Id name) {
      AmbTypeNode_c n = (AmbTypeNode_c) copy();
      n.name = name;
      return n;
  }
  
  public String name() {
    return this.name.id();
  }

  public AmbTypeNode name(String name) {
      return id(this.name.id(name));
  }

  public QualifierNode qual() {
    return this.qual;
  }

  public AmbTypeNode qual(QualifierNode qual) {
    AmbTypeNode_c n = (AmbTypeNode_c) copy();
    n.qual = qual;
    return n;
  }

  protected AmbTypeNode_c reconstruct(QualifierNode qual, Id name) {
    if (qual != this.qual || name != this.name) {
      AmbTypeNode_c n = (AmbTypeNode_c) copy();
      n.qual = qual;
      n.name = name;
      return n;
    }

    return this;
  }

  public Node buildTypes(TypeBuilder tb) throws SemanticException {
    return type(tb.typeSystem().unknownType(position()));
  }

  public Node visitChildren(NodeVisitor v) {
      QualifierNode qual = (QualifierNode) visitChild(this.qual, v);
      Id name = (Id) visitChild(this.name, v);
      return reconstruct(qual, name);
  }

  public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
      if (qual != null && ! qual.isDisambiguated()) {
          return this;
      }

      Node n = sc.nodeFactory().disamb().disambiguate(this, sc, position(), qual,
                                                      name);
      
      if (n instanceof TypeNode) {
          return n;
      }
      
      throw new SemanticException("Could not find type \"" +
                                  (qual == null ? name.toString() : qual.toString() + "." + name.toString()) +
                                  "\".", position());
  }

  public Node typeCheck(TypeChecker tc) throws SemanticException {
      // Didn't finish disambiguation; just return.
      return this;
  }

  public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
    throw new InternalCompilerError(position(),
                                    "Cannot exception check ambiguous node "
                                    + this + ".");
  } 

  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
    if (qual != null) {
        print(qual, w, tr);
        w.write(".");
	w.allowBreak(2, 3, "", 0);
    }
            
    tr.print(this, name, w);
  }

  public String toString() {
    return (qual == null
            ? name.toString()
            : qual.toString() + "." + name.toString()) + "{amb}";
  }
  public Node copy(NodeFactory nf) {
      return nf.AmbTypeNode(this.position, this.qual, this.name);
  }
}
