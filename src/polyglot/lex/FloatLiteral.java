package jltools.lex;

import java_cup.runtime.Symbol;
import jltools.util.Position;

public class FloatLiteral extends NumericLiteral {
  public FloatLiteral(Position position, float f, int sym) {
      super(position, sym);
      this.val = new Float(f);
  }
}
