package jltools.lex;

import jltools.util.Position;

public abstract class NumericLiteral extends Literal {
  Number val;

  public NumericLiteral(Position position, int sym) { super(position, sym); }

  public Number getValue() { return val; }

  public String toString() { return "NumericLiteral <"+val.toString()+">"; }
}
