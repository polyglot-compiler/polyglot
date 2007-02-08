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

import java.util.*;

/**
 * An <code>AmbExpr</code> is an ambiguous AST node composed of a single
 * identifier that must resolve to an expression.
 */
public class AmbExpr_c extends Expr_c implements AmbExpr
{
  protected Id name;

  public AmbExpr_c(Position pos, Id name) {
    super(pos);
    assert(name != null);
    this.name = name;
  }

  /** Get the precedence of the field. */
  public Precedence precedence() {
    return Precedence.LITERAL;
  }
  
  /** Get the name of the expression. */
  public Id id() {
      return this.name;
  }
  
  /** Set the name of the expression. */
  public AmbExpr id(Id id) {
      AmbExpr_c n = (AmbExpr_c) copy();
      n.name = id;
      return n;
  }

  /** Get the name of the expression. */
  public String name() {
    return this.name.id();
  }

  /** Set the name of the expression. */
  public AmbExpr name(String name) {
      return id(this.name.id(name));
  }
  
  /** Reconstruct the expression. */
  protected AmbExpr_c reconstruct(Id name) {
      if (name != this.name) {
          AmbExpr_c n = (AmbExpr_c) copy();
          n.name = name;
          return n;
      }
      return this;
  }
  
  /** Visit the children of the constructor. */
  public Node visitChildren(NodeVisitor v) {
      Id name = (Id) visitChild(this.name, v);
      return reconstruct(name);
  }

  /** Disambiguate the expression. */
  public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
    Node n = ar.nodeFactory().disamb().disambiguate(this, ar, position(),
                                                    null, name);

    if (n instanceof Expr) {
      return n;
    }

    throw new SemanticException("Could not find field or local " +
                                "variable \"" + name + "\".", position());
  }

  public Node typeCheck(TypeChecker tc) throws SemanticException {
      // Didn't finish disambiguation; just return.
      return this;
  }

  /** Check exceptions thrown by the expression. */
  public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
    throw new InternalCompilerError(position(),
                                    "Cannot exception check ambiguous node "
                                    + this + ".");
  } 

  /** Write the expression to an output file. */
  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
      tr.print(this, name, w);
  }

  public String toString() {
    return name.toString() + "{amb}";
  }

  /**
   * Return the first (sub)term performed when evaluating this
   * term.
   */
  public Term entry() {
      return this;
  }

  /**
   * Visit this term in evaluation order.
   */
  public List acceptCFG(CFGBuilder v, List succs) {
      return succs;
  }
}
