package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Options;

/**
 * A <code>CanonicalTypeNode</code> is a type node for a canonical type.
 */
public class CanonicalTypeNode_c extends TypeNode_c implements CanonicalTypeNode
{
  public CanonicalTypeNode_c(Del ext, Position pos, Type type) {
    super(ext, pos);
    this.type = type;
  }

  /**
   * If the "use-fully-qualified-class-names" options is used, then the
   * fully qualified names is written out (<code>java.lang.Object</code>).
   * Otherwise, the string that originally represented the type in the
   * source file is used.
   */
  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
    if (type == null) w.write("<unknown-type>");
    else w.write(type.toString());
  }

  public void translate(CodeWriter w, Translator tr) {
    TypeSystem ts = tr.typeSystem();

    if (tr.outerClass() != null) {
      w.write(type.translate(ts.classContextResolver(tr.outerClass())));
    }
    else if (! Options.global.fully_qualified_names) {
      w.write(type.translate(tr.context()));
    }
    else {
      w.write(type.translate(null));
    }
  }

  /*
  public String toString() {
    return type.toString();
  }
  */

  public void dump(CodeWriter w) {
    super.dump(w);
    w.allowBreak(4, " ");
    w.begin(0);
    w.write("(type " + type + ")");
    w.end();
  }
}
