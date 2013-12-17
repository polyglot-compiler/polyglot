package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.ConstantChecker;
import polyglot.visit.TypeChecker;

public class JL7Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static JL7Ext ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof JL7Ext)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No JL7 extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (JL7Ext) e;
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return this.superDel().typeCheck(this.node(), tc);
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        return this.superDel().checkConstants(this.node(), cc);
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        return this.superDel().throwTypes(this.node(), ts);
    }
}
