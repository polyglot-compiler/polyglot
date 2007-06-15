/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import java.util.*;

/**
 * A <code>FieldAssign_c</code> represents a Java assignment expression to
 * a field.  For instance, <code>this.x = e</code>.
 * 
 * The class of the <code>Expr</code> returned by
 * <code>FieldAssign_c.left()</code>is guaranteed to be a <code>Field</code>.
 */
public class FieldAssign_c extends Assign_c implements FieldAssign
{
  public FieldAssign_c(Position pos, Field left, Operator op, Expr right) {
    super(pos, left, op, right);
  }

  public Assign left(Expr left) {
      FieldAssign_c n = (FieldAssign_c)super.left(left);
      n.assertLeftType();
      return n;
  }

  private void assertLeftType() {
      if (!(left() instanceof Field)) {
          throw new InternalCompilerError("left expression of an FieldAssign must be a field");
      }
  }
  
  public Term firstChild() {
      Field f = (Field)left();
      if (f.target() instanceof Expr) {
          return ((Expr) f.target());
      }
      else {
          if (operator() != Assign.ASSIGN) {
              return f;
          }
          else {
              return right();
          }
      }
  }
  
  protected void acceptCFGAssign(CFGBuilder v) {
      Field f = (Field)left();
      if (f.target() instanceof Expr) {
          Expr o = (Expr) f.target();

              //     o.f = e: visit o -> e -> (o.f = e)
              v.visitCFG(o, right(), true);              
              v.visitCFG(right(), this, false);
      }
      else {
              //       T.f = e: visit e -> (T.f OP= e)
              v.visitCFG(right(), this, false);
      }
      
  }
  protected void acceptCFGOpAssign(CFGBuilder v) {
      /*
      Field f = (Field)left();
      if (f.target() instanceof Expr) {
          Expr o = (Expr) f.target();

          // o.f OP= e: visit o -> o.f -> e -> (o.f OP= e)
          v.visitCFG(o, f);
          v.visitThrow(f);
          v.edge(f, right().entry());
          v.visitCFG(right(), this);
      }
      else {
          // T.f OP= e: visit T.f -> e -> (T.f OP= e)
          v.visitThrow(f);
          v.edge(f, right().entry());
          v.visitCFG(right(), this);
      }
      */
      
      v.visitCFG(left(), right(), true);
      v.visitCFG(right(), this, false);
  }

  public List throwTypes(TypeSystem ts) {
      List l = new ArrayList(super.throwTypes(ts));

      Field f = (Field)left();
      if (f.target() instanceof Expr) {
          l.add(ts.NullPointerException());
      }

      return l;
  }  
  
}
