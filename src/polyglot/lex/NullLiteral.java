package jltools.lex;

import java_cup.runtime.Symbol;
import jltools.util.Position;

/** A token class for null literals. */
public class NullLiteral extends Literal {
  public NullLiteral(Position position, int sym) { super(position, sym); }
  public String toString() { return "NullLiteral <null>"; }
}
