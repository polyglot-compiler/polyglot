package jltools.lex;

import java_cup.runtime.Symbol;
import jltools.util.Position;

public class LongLiteral extends NumericLiteral {
  public LongLiteral(Position position, long l, int sym) {
      super(position, sym);
      this.val = new Long(l);
  }
}
