package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/** 
 * A <code>StringLiteral</code> represents an immutable instance of a 
 * <code>String</code> which corresponds to a literal string in Java code.
 */
public class StringLiteral extends Literal 
{
  protected final String value;

  /**
   * Creates a new <code>StringLiteral</code>.
   */ 
  public StringLiteral( Node ext, String value) 
  {
    this.ext = ext;
    this.value = value;
  }

    public StringLiteral(String value) {
	this(null, value);
    }

    public StringLiteral reconstruct( Node ext, String value) {
	if (this.ext == ext && this.value.equals(value)) {
	    return this;
	} else {
	    StringLiteral n = new StringLiteral(ext, value);
	    n.copyAnnotationsFrom(this);
	    return n;
	}
    }

    public StringLiteral reconstruct(String value) {
	return reconstruct(this.ext, value);
    }

  
  /**
   * Returns the string value of this node.
   */ 
  public String getString() 
  {
    return value;
  }

  public Node visitChildren( NodeVisitor v) 
  {
      return reconstruct(Node.condVisit(ext, v), value);
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    setCheckedType( c.getTypeSystem().getString());
    return this;
  }
  
  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "\"");
    for (int i = 0; i < value.length(); i++) {
	int ch = value.charAt(i);
	if (ch > 0xFF) {
	    w.write(""+(char)ch);
	} else {
	    switch (ch) {
		case '\b': w.write("\\b"); break;
		case '\t': w.write("\\t"); break;
		case '\n': w.write("\\n"); break;
		case '\f': w.write("\\f"); break;
		case '\r': w.write("\\r"); break;
		case '\"': w.write("\\\""); break;
		case '\'': w.write("'"); break;
		case '\\': w.write("\\\\"); break;
		default:
		    if (ch >= 0x20 && ch < 0x7F) {
			w.write("" + (char)ch);
		    } else {
			w.write("\\");
			w.write("" + (char)('0' + ch/64));
			w.write("" + (char)('0' + (ch&63)/8));
			w.write("" + (char)('0' + (ch&7)));
		    }
	    }
	}
    }
    w.write("\"");
  }

  public void dump( CodeWriter w)
  {
    w.write( "( STRING LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
