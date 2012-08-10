package polyglot.translate.ext;

import polyglot.ast.BooleanLit;
import polyglot.ast.CharLit;
import polyglot.ast.ClassLit;
import polyglot.ast.FloatLit;
import polyglot.ast.IntLit;
import polyglot.ast.Lit;
import polyglot.ast.Node;
import polyglot.ast.NullLit;
import polyglot.ast.StringLit;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;

public class LitToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Lit n = (Lit) node();
        if (n instanceof BooleanLit) {
            BooleanLit l = (BooleanLit) n;
            return rw.to_nf().BooleanLit(n.position(), l.value());
        }
        else if (n instanceof ClassLit) {
            ClassLit l = (ClassLit) n;
            return rw.to_nf().ClassLit(n.position(), l.typeNode());
        }
        else if (n instanceof FloatLit) {
            FloatLit l = (FloatLit) n;
            return rw.to_nf().FloatLit(n.position(), l.kind(), l.value());
        }
        else if (n instanceof NullLit) {
            return rw.to_nf().NullLit(n.position());
        }
        else if (n instanceof CharLit) {
            CharLit l = (CharLit) n;
            return rw.to_nf().CharLit(n.position(), l.value());
        }
        else if (n instanceof IntLit) {
            IntLit l = (IntLit) n;
            return rw.to_nf().IntLit(n.position(), l.kind(), l.value());
        }
        else if (n instanceof StringLit) {
            StringLit l = (StringLit) n;
            return rw.to_nf().StringLit(n.position(), l.value());
        }
        else {
            throw new InternalCompilerError("Unexpected lit");
        }
    }
}
