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
 * A <code>AmbAssign</code> represents a Java assignment expression to
 * an as yet unknown expression.
 */
public class AmbAssign_c extends Assign_c implements AmbAssign
{
  public AmbAssign_c(Position pos, Expr left, Operator op, Expr right) {
    super(pos, left, op, right);
  }
  
  public Term firstChild() {
    if (operator() != Assign.ASSIGN) {
      return left();
    }

    return right();
  }
  
  protected void acceptCFGAssign(CFGBuilder v) {
      v.visitCFG(right(), this, false);
  }
  
  protected void acceptCFGOpAssign(CFGBuilder v) {
      v.visitCFG(left(), right(), true);
      v.visitCFG(right(), this, false);
  }
  
  public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
      Assign n = (Assign) super.disambiguate(ar);
      
      if (n.left() instanceof Local) {
          return ar.nodeFactory().LocalAssign(n.position(), (Local)left(), operator(), right());
      }
      else if (n.left() instanceof Field) {
          return ar.nodeFactory().FieldAssign(n.position(), (Field)left(), operator(), right());
      } 
      else if (n.left() instanceof ArrayAccess) {
          return ar.nodeFactory().ArrayAccessAssign(n.position(), (ArrayAccess)left(), operator(), right());
      }

      // LHS is still ambiguous.  The pass should get rerun later.
      return this;
      // throw new SemanticException("Could not disambiguate left side of assignment!", n.position());
  }
  

  public Node typeCheck(TypeChecker tc) throws SemanticException {
      // Didn't finish disambiguation; just return.
      return this;
  }
  public Node copy(NodeFactory nf) {
      return nf.AmbAssign(this.position, this.left, this.op, this.right);
  }
}
