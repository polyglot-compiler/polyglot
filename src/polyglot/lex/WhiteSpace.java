package jltools.lex;

class WhiteSpace extends InputElement {
  char whitespace;
  WhiteSpace(char which) { this.whitespace=which; }

  public String toString() {
    String s;
    switch(whitespace) {
    case ' ':  s = "SP"; break;
    case '\t': s = "HT"; break;
    case '\f': s = "FF"; break;
    case '\n': s = "LT"; break;
    default:   s = "Unknown Whitespace character."; break;
    }
    return "Whitespace <"+s+">";
  }
}
