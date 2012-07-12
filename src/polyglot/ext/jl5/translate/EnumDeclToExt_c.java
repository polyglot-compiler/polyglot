package polyglot.ext.jl5.translate;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.JL5EnumDecl;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ClassDeclToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.Flags;
import polyglot.types.SemanticException;

public class EnumDeclToExt_c extends ClassDeclToExt_c implements ToExt {
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        JL5EnumDecl cd = (JL5EnumDecl) node();
		return ((JL5NodeFactory) rw.to_nf()).EnumDecl(cd.position(),
				cd.flags(), cd.annotations(), cd.id(), cd.superClass(),
				cd.interfaces(), cd.body());
    }
}
