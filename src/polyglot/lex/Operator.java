package Lex;

import java.util.Hashtable;
import java_cup.runtime.Symbol;

class Operator extends Token {
  String which;
  Operator(String which) { this.which = which; }

  public String toString() { return "Operator <"+which+">"; }

  Symbol token() { 
    Integer i = (Integer) op_table.get(which);
    return new Symbol(i.intValue()); 
  }

  static private final Hashtable op_table = new Hashtable();
  static {
    op_table.put("=", new Integer(Sym.EQ));
    op_table.put(">", new Integer(Sym.GT));
    op_table.put("<", new Integer(Sym.LT));
    op_table.put("!", new Integer(Sym.NOT));
    op_table.put("~", new Integer(Sym.COMP));
    op_table.put("?", new Integer(Sym.QUESTION));
    op_table.put(":", new Integer(Sym.COLON));
    op_table.put("==", new Integer(Sym.EQEQ));
    op_table.put("<=", new Integer(Sym.LTEQ));
    op_table.put(">=", new Integer(Sym.GTEQ));
    op_table.put("!=", new Integer(Sym.NOTEQ));
    op_table.put("&&", new Integer(Sym.ANDAND));
    op_table.put("||", new Integer(Sym.OROR));
    op_table.put("++", new Integer(Sym.PLUSPLUS));
    op_table.put("--", new Integer(Sym.MINUSMINUS));
    op_table.put("+", new Integer(Sym.PLUS));
    op_table.put("-", new Integer(Sym.MINUS));
    op_table.put("*", new Integer(Sym.MULT));
    op_table.put("/", new Integer(Sym.DIV));
    op_table.put("&", new Integer(Sym.AND));
    op_table.put("|", new Integer(Sym.OR));
    op_table.put("^", new Integer(Sym.XOR));
    op_table.put("%", new Integer(Sym.MOD));
    op_table.put("<<", new Integer(Sym.LSHIFT));
    op_table.put(">>", new Integer(Sym.RSHIFT));
    op_table.put(">>>", new Integer(Sym.URSHIFT));
    op_table.put("+=", new Integer(Sym.PLUSEQ));
    op_table.put("-=", new Integer(Sym.MINUSEQ));
    op_table.put("*=", new Integer(Sym.MULTEQ));
    op_table.put("/=", new Integer(Sym.DIVEQ));
    op_table.put("&=", new Integer(Sym.ANDEQ));
    op_table.put("|=", new Integer(Sym.OREQ));
    op_table.put("^=", new Integer(Sym.XOREQ));
    op_table.put("%=", new Integer(Sym.MODEQ));
    op_table.put("<<=", new Integer(Sym.LSHIFTEQ));
    op_table.put(">>=", new Integer(Sym.RSHIFTEQ));
    op_table.put(">>>=", new Integer(Sym.URSHIFTEQ));
  }
}
