package polyglot.ext.jl5.translate;

import polyglot.ast.Call;
import polyglot.ast.Cast;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.CallToExt_c;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class JL5CallToJL_c extends CallToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Call n = (Call) super.toExt(rw);

        Call orig = (Call) this.node();

        // Check if the call is to clone() on an array, if so add a cast
        if (n.name().equals("clone")) {
            MethodInstance mi = orig.methodInstance();
            if (mi == null) {
                // we don't have the type information.  It is the responsibility 
                // of the creator to add the cast if the target is an array object.
                return n;
            }
            Type ct = orig.methodInstance().container();
            if (ct == null) {
                // we don't have the type information.  It is the responsibility 
                // of the creator to add the cast if the target is an array object.
                return n;
            }
            if (ct.isArray()) {
                // it's a call to clone() on an array object of type T[].
                // We need to cast the result to T[], since in JL5 the
                // return type is T[] (see JLS 3rd ed. 6.4.5), but in
                // Java 1.4 it is Object.
                Cast c =
                        rw.to_nf()
                          .Cast(Position.compilerGenerated(),
                                rw.typeToJava(ct, Position.compilerGenerated()),
                                n);
                return c;
            }
        }

        return n;
    }
}
