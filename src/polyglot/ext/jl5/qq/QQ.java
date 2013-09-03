package polyglot.ext.jl5.qq;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.ExtensionInfo;
import polyglot.lex.Lexer;
import polyglot.qq.QQParser;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;

public class QQ extends polyglot.qq.QQ {

    public QQ(ExtensionInfo ext) {
        super(ext);
    }

    public QQ(ExtensionInfo ext, Position pos) {
        super(ext, pos);
    }

    @Override
    protected QQParser parser(Lexer lexer, TypeSystem ts, NodeFactory nf,
            ErrorQueue eq) {
        return new polyglot.ext.jl5.qq.Grm(lexer, ts, nf, eq);
    }
}
