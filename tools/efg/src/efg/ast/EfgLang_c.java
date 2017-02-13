package efg.ast;

import polyglot.ast.ClassDecl;
import polyglot.ast.ClassDeclOps;
import polyglot.ast.Ext;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyglot.ext.jl7.ast.J7Lang_c;
import polyglot.util.InternalCompilerError;

public class EfgLang_c extends J7Lang_c implements EfgLang {
    public static final EfgLang_c INSTANCE = new EfgLang_c();

    public static EfgLang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof EfgLang) return (EfgLang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }

    protected EfgLang_c() {
    }

    protected static EfgExt efgExt(Node n) {
        return EfgExt.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return efgExt(n);
    }

    @Override
    protected ClassDeclOps ClassDeclOps(ClassDecl n) {
        return (ClassDeclOps) efgExt(n);
    }

}
