package jltools.lex;

abstract class NumericLiteral extends Literal {
  Number val;

  public NumericLiteral(int line) { super(line); }

  public String toString() { return "NumericLiteral <"+val.toString()+">"; }
}
