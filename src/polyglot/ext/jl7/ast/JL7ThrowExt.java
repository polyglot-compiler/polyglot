package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.Local;
import polyglot.ast.Throw;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;

public class JL7ThrowExt extends JL7Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<Type> throwSet = null;

    public List<Type> throwTypes(TypeSystem ts) {
        Throw n = (Throw) this.node();
        if (n.expr() instanceof Local) {
            Local local = (Local) n.expr();
            JL5LocalInstance li = (JL5LocalInstance) local.localInstance();
            if (this.throwSet != null) {
                // this is a rethrow of a final (or implicitly final)
                // formal of a catch block. See JLS7 11.2.2.
                // The throwSet field is set by the ExceptionChecker analysis
                // via the code in JL7TryExt. 
                return this.throwSet;
            }
        }
        return this.superDel().throwTypes(ts);
    }

}
