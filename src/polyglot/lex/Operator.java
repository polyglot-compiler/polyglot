package jltools.lex;

import java.util.Hashtable;
import jltools.util.Position;
import java_cup.runtime.Symbol;

public class Operator extends Token {
  String which;
  public Operator(Position position, String which, int sym) {
      super(position, sym);
      this.which = which;
  }

  public String toString() { return "Operator <"+which+">"; }
}
