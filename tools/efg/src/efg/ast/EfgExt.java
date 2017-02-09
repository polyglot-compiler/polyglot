package efg.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class EfgExt extends Ext_c {
  private static final long serialVersionUID = SerialVersionUID.generate();

  public static EfgExt ext(Node n) {
    Ext e = n.ext();
    while (e != null && !(e instanceof EfgExt)) {
      e = e.ext();
    }
    if (e == null) {
      throw new InternalCompilerError(
          "No Efg extension object for node " + n + " (" + n.getClass() + ")",
          n.position());
    }
    return (EfgExt) e;
  }

  @Override
  public final EfgLang lang() {
    return EfgLang_c.INSTANCE;
  }
}
