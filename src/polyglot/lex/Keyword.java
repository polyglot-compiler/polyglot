package jltools.lex;

import java.util.Hashtable;
import java_cup.runtime.Symbol;

class Keyword extends Token {
  String keyword;
  Keyword(int line, String s) { super(line); keyword = s; }

  public Symbol symbol() {
    Integer i = (Integer) key_table.get(keyword);
    return new Symbol(i.intValue(), this);
  }
  public String toString() { return "Keyword <"+keyword+">"; }

  static private final Hashtable key_table = new Hashtable();
  static {
    key_table.put("abstract", new Integer(jltools.parse.sym.ABSTRACT));
    key_table.put("boolean", new Integer(jltools.parse.sym.BOOLEAN));
    key_table.put("break", new Integer(jltools.parse.sym.BREAK));
    key_table.put("byte", new Integer(jltools.parse.sym.BYTE));
    key_table.put("case", new Integer(jltools.parse.sym.CASE));
    key_table.put("catch", new Integer(jltools.parse.sym.CATCH));
    key_table.put("char", new Integer(jltools.parse.sym.CHAR));
    key_table.put("class", new Integer(jltools.parse.sym.CLASS));
    key_table.put("const", new Integer(jltools.parse.sym.CONST));
    key_table.put("continue", new Integer(jltools.parse.sym.CONTINUE));
    key_table.put("default", new Integer(jltools.parse.sym.DEFAULT));
    key_table.put("do", new Integer(jltools.parse.sym.DO));
    key_table.put("double", new Integer(jltools.parse.sym.DOUBLE));
    key_table.put("else", new Integer(jltools.parse.sym.ELSE));
    key_table.put("extends", new Integer(jltools.parse.sym.EXTENDS));
    key_table.put("final", new Integer(jltools.parse.sym.FINAL));
    key_table.put("finally", new Integer(jltools.parse.sym.FINALLY));
    key_table.put("float", new Integer(jltools.parse.sym.FLOAT));
    key_table.put("for", new Integer(jltools.parse.sym.FOR));
    key_table.put("goto", new Integer(jltools.parse.sym.GOTO));
    key_table.put("if", new Integer(jltools.parse.sym.IF));
    key_table.put("implements", new Integer(jltools.parse.sym.IMPLEMENTS));
    key_table.put("import", new Integer(jltools.parse.sym.IMPORT));
    key_table.put("instanceof", new Integer(jltools.parse.sym.INSTANCEOF));
    key_table.put("int", new Integer(jltools.parse.sym.INT));
    key_table.put("interface", new Integer(jltools.parse.sym.INTERFACE));
    key_table.put("long", new Integer(jltools.parse.sym.LONG));
    key_table.put("native", new Integer(jltools.parse.sym.NATIVE));
    key_table.put("new", new Integer(jltools.parse.sym.NEW));
    key_table.put("package", new Integer(jltools.parse.sym.PACKAGE));
    key_table.put("private", new Integer(jltools.parse.sym.PRIVATE));
    key_table.put("protected", new Integer(jltools.parse.sym.PROTECTED));
    key_table.put("public", new Integer(jltools.parse.sym.PUBLIC));
    key_table.put("return", new Integer(jltools.parse.sym.RETURN));
    key_table.put("short", new Integer(jltools.parse.sym.SHORT));
    key_table.put("static", new Integer(jltools.parse.sym.STATIC));
    key_table.put("strictfp", new Integer(jltools.parse.sym.STRICTFP));
    key_table.put("super", new Integer(jltools.parse.sym.SUPER));
    key_table.put("switch", new Integer(jltools.parse.sym.SWITCH));
    key_table.put("synchronized", new Integer(jltools.parse.sym.SYNCHRONIZED));
    key_table.put("this", new Integer(jltools.parse.sym.THIS));
    key_table.put("throw", new Integer(jltools.parse.sym.THROW));
    key_table.put("throws", new Integer(jltools.parse.sym.THROWS));
    key_table.put("transient", new Integer(jltools.parse.sym.TRANSIENT));
    key_table.put("try", new Integer(jltools.parse.sym.TRY));
    key_table.put("void", new Integer(jltools.parse.sym.VOID));
    key_table.put("volatile", new Integer(jltools.parse.sym.VOLATILE));
    key_table.put("while", new Integer(jltools.parse.sym.WHILE));
  }
}
