package polyglot.ext.jl8.ast;

import polyglot.ext.jl7.ast.JL7NodeFactory_c;

/**
 * NodeFactory for src/polyglot/ext/jl8 extension.
 */
public class JL8NodeFactory_c extends JL7NodeFactory_c implements JL8NodeFactory {
    public JL8NodeFactory_c(JL8Lang lang, JL8ExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public JL8ExtFactory extFactory() {
        return (JL8ExtFactory) super.extFactory();
    }

    // TODO:  Implement factory methods for new AST nodes.
    // TODO:  Override factory methods for overridden AST nodes.
    // TODO:  Override factory methods for AST nodes with new extension nodes.
}
