package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>Assign</code> represents a Java assignment expression.
 */
public class Assign_c extends Expr_c implements Assign
{
  protected Expr left;
  protected Operator op;
  protected Expr right;

  public Assign_c(Ext ext, Position pos, Expr left, Operator op, Expr right) {
    super(ext, pos);
    this.left = left;
    this.op = op;
    this.right = right;
  }

  /** Get the precedence of the expression. */
  public Precedence precedence() {
    return Precedence.ASSIGN;
  }

  /** Get the left operand of the expression. */
  public Expr left() {
    return this.left;
  }

  /** Set the left operand of the expression. */
  public Assign left(Expr left) {
    Assign_c n = (Assign_c) copy();
    n.left = left;
    return n;
  }

  /** Get the operator of the expression. */
  public Operator operator() {
    return this.op;
  }

  /** Set the operator of the expression. */
  public Assign operator(Operator op) {
    Assign_c n = (Assign_c) copy();
    n.op = op;
    return n;
  }

  /** Get the right operand of the expression. */
  public Expr right() {
    return this.right;
  }

  /** Set the right operand of the expression. */
  public Assign right(Expr right) {
    Assign_c n = (Assign_c) copy();
    n.right = right;
    return n;
  }

  /** Reconstruct the expression. */
  protected Assign_c reconstruct(Expr left, Expr right) {
    if (left != this.left || right != this.right) {
      Assign_c n = (Assign_c) copy();
      n.left = left;
      n.right = right;
      return n;
    }

    return this;
  }

  /** Visit the children of the expression. */
  public Node visitChildren(NodeVisitor v) {
    Expr left = (Expr) this.left.visit(v);
    Expr right = (Expr) this.right.visit(v);
    return reconstruct(left, right);
  }

  /** Type check the expression. */
  public Node typeCheck_(TypeChecker tc) throws SemanticException {
    Type t = left.type();
    Type s = right.type();

    TypeSystem ts = tc.typeSystem();

    boolean intConversion = false;

    if (right instanceof NumLit) {
      long value = ((NumLit) right).longValue();
      intConversion = ts.numericConversionValid(t, value);
    }

    if (op == ASSIGN) {
      if (! s.isAssignableSubtype(t) &&
          ! s.isSame(t) &&
          ! intConversion) {

        throw new SemanticException("Cannot assign " + s + " to " + t + ".",
                                    position());
      }

      return type(t);
    }

    if (op == ADD_ASSIGN) {
      if (t.isSame(ts.String()) || s.isSame(ts.String())) {
        return type(ts.String());
      }
    }

    if (op == BIT_AND_ASSIGN || op == BIT_OR_ASSIGN ||
        op == BIT_XOR_ASSIGN) {
      if (t.isBoolean() && s.isBoolean()) {
        return type(ts.Boolean());
      }
    }

    if (! t.isNumeric() || ! s.isNumeric()) {
      if (op == ADD_ASSIGN) {
        throw new SemanticException("The " + op + " operator must have "
                                    + "numeric or String operands.",
                                    position());
      }

      if (op == BIT_AND_ASSIGN || op == BIT_OR_ASSIGN ||
          op == BIT_XOR_ASSIGN) {
        throw new SemanticException("The " + op + " operator must have "
                                    + "numeric or boolean operands.",
                                    position());
      }

      if (op == SUB_ASSIGN || op == MUL_ASSIGN ||
          op == DIV_ASSIGN || op == MOD_ASSIGN ||
          op == SHL_ASSIGN || op == SHR_ASSIGN ||
          op == USHR_ASSIGN) {
        throw new SemanticException("The " + op + " operator must have "
                                    + "numeric operands.",
                                    position());
      }

      throw new InternalCompilerError("Unrecognized assignment operator " +
                                      op + ".");
    }

    if (op == SHL_ASSIGN || op == SHR_ASSIGN || op == USHR_ASSIGN) {
      // Only promote the left of a shift.
      return type(ts.promote(t));
    }
      
    return type(ts.promote(t, s));
  }
  
  /** Check exceptions thrown by the expression. */
  public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
    TypeSystem ts = ec.typeSystem();

    if (throwsArithmeticException()) {
      ec.throwsException(ts.ArithmeticException());
    }

    if (throwsArrayStoreException()) {
      ec.throwsException(ts.ArrayStoreException());
    }

    return this;
  }

  /** Get the throwsArrayStoreException of the expression. */
  public boolean throwsArrayStoreException() {
    return op == ASSIGN &&
      left.type().isReference() &&
      left instanceof ArrayAccess;
  }

  /** Get the throwsArithmeticException of the expression. */
  public boolean throwsArithmeticException() {
    // conservatively assume that any division or mod may throw
    // ArithmeticException this is NOT true-- floats and doubles don't
    // throw any exceptions ever...
    return op == DIV_ASSIGN || op == MOD_ASSIGN;
  }

  public String toString() {
    return left + " " + op + " " + right;
  }

  /** Write the expression to an output file. */
  public void translate_(CodeWriter w, Translator tr) {
    translateSubexpr(left, w, tr);
    w.write(" ");
    w.write(op.toString());
    w.allowBreak(2, " ");
    translateSubexpr(right, w, tr);
  }

  /** Dumps the AST. */
  public void dump(CodeWriter w) {
    super.dump(w);
    w.allowBreak(4, " ");
    w.begin(0);
    w.write("(operator " + op + ")");
    w.end();
  }
}
