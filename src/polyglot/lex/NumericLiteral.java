package jltools.lex;

public abstract class NumericLiteral extends Literal {
  Number val;

  public NumericLiteral(int line) { super(line); }

  public Number getValue() { return val; }

  public String toString() { return "NumericLiteral <"+val.toString()+">"; }
}
