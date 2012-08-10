package polyglot.translate.ext;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.visit.NodeVisitor;

public interface ToExt extends Ext {

    Node toExt(ExtensionRewriter extensionRewriter) throws SemanticException;

    NodeVisitor toExtEnter(ExtensionRewriter extensionRewriter)
            throws SemanticException;

}
