package jltools.lex;

import java_cup.runtime.Symbol;
import jltools.util.Position;

/** A token class for string literals. */
public class StringLiteral extends Literal {
  String val;
  public StringLiteral(Position position, String s, int sym) {
      super(position, sym);
      this.val = s;
  }

  public String getValue() { return val; }

  public String toString() {
    return "StringLiteral <"+Token.escape(val)+">";
  }
}
