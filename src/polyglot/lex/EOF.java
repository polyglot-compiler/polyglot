package jltools.lex;

import java_cup.runtime.Symbol;
import jltools.util.Position;

public class EOF extends Token {
  public EOF(Position position, int sym) { super(position, sym); }
  public String toString() { return "EOF"; }
}
