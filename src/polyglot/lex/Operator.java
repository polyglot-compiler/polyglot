package jltools.lex;

import java.util.Hashtable;
import java_cup.runtime.Symbol;

class Operator extends Token {
  String which;
  Operator(int line, String which) { super(line); this.which = which; }

  public String toString() { return "Operator <"+which+">"; }

  public Symbol symbol() {
    Integer i = (Integer) op_table.get(which);
    return new Symbol(i.intValue(), this);
  }

  static private final Hashtable op_table = new Hashtable();
  static {
    op_table.put("=", new Integer(jltools.parse.sym.EQ));
    op_table.put(">", new Integer(jltools.parse.sym.GT));
    op_table.put("<", new Integer(jltools.parse.sym.LT));
    op_table.put("!", new Integer(jltools.parse.sym.NOT));
    op_table.put("~", new Integer(jltools.parse.sym.COMP));
    op_table.put("?", new Integer(jltools.parse.sym.QUESTION));
    op_table.put(":", new Integer(jltools.parse.sym.COLON));
    op_table.put("==", new Integer(jltools.parse.sym.EQEQ));
    op_table.put("<=", new Integer(jltools.parse.sym.LTEQ));
    op_table.put(">=", new Integer(jltools.parse.sym.GTEQ));
    op_table.put("!=", new Integer(jltools.parse.sym.NOTEQ));
    op_table.put("&&", new Integer(jltools.parse.sym.ANDAND));
    op_table.put("||", new Integer(jltools.parse.sym.OROR));
    op_table.put("++", new Integer(jltools.parse.sym.PLUSPLUS));
    op_table.put("--", new Integer(jltools.parse.sym.MINUSMINUS));
    op_table.put("+", new Integer(jltools.parse.sym.PLUS));
    op_table.put("-", new Integer(jltools.parse.sym.MINUS));
    op_table.put("*", new Integer(jltools.parse.sym.MULT));
    op_table.put("/", new Integer(jltools.parse.sym.DIV));
    op_table.put("&", new Integer(jltools.parse.sym.AND));
    op_table.put("|", new Integer(jltools.parse.sym.OR));
    op_table.put("^", new Integer(jltools.parse.sym.XOR));
    op_table.put("%", new Integer(jltools.parse.sym.MOD));
    op_table.put("<<", new Integer(jltools.parse.sym.LSHIFT));
    op_table.put(">>", new Integer(jltools.parse.sym.RSHIFT));
    op_table.put(">>>", new Integer(jltools.parse.sym.URSHIFT));
    op_table.put("+=", new Integer(jltools.parse.sym.PLUSEQ));
    op_table.put("-=", new Integer(jltools.parse.sym.MINUSEQ));
    op_table.put("*=", new Integer(jltools.parse.sym.MULTEQ));
    op_table.put("/=", new Integer(jltools.parse.sym.DIVEQ));
    op_table.put("&=", new Integer(jltools.parse.sym.ANDEQ));
    op_table.put("|=", new Integer(jltools.parse.sym.OREQ));
    op_table.put("^=", new Integer(jltools.parse.sym.XOREQ));
    op_table.put("%=", new Integer(jltools.parse.sym.MODEQ));
    op_table.put("<<=", new Integer(jltools.parse.sym.LSHIFTEQ));
    op_table.put(">>=", new Integer(jltools.parse.sym.RSHIFTEQ));
    op_table.put(">>>=", new Integer(jltools.parse.sym.URSHIFTEQ));
  }
}
