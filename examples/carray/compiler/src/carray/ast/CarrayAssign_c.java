package jltools.ext.carray.ast;

import jltools.ext.carray.types.*;
import jltools.ext.jl.ast.*;
import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>Assign</code> represents a Java assignment expression.
 * This class extends Assign_c to implement the restriction that
 * elements of a const array cannot be modified.
 */
public class CarrayAssign_c extends Assign_c
{
  public CarrayAssign_c(Del ext, Position pos, Expr left, Operator op, Expr right) {
    super(ext, pos, left, op, right);
  }

  /**
   * Type check the expression.
   * The only change is that elements of a const array cannot be modified.
   *
   */
  public Node typeCheck(TypeChecker tc) throws SemanticException {
    CarrayTypeSystem ts = (CarrayTypeSystem)tc.typeSystem();

    Type t = left.type();

    // check that the left is an assignable location.
     if (left instanceof ArrayAccess && ((ArrayAccess)left).array().type() instanceof ConstArrayType) {
        throw new SemanticException("Cannot assign a value to an element of a const array.",
                                    position());
    }

    // let the super class deal with the rest.
    return super.typeCheck(tc);
  }
}
