package polyglot.translate.ext;

import polyglot.ast.*;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.*;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

public class FieldDeclToExt_c extends ToExt_c {
	protected FieldInstance fi = null;

	public Node toExt(ExtensionRewriter rw) throws SemanticException {
		FieldDecl n = (FieldDecl) node();
		return rw.to_nf().FieldDecl(n.position(), n.flags(), n.type(), n.id(),
				n.init());
	}
}
