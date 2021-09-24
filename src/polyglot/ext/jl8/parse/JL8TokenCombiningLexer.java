package polyglot.ext.jl8.parse;

import java.io.IOException;
import polyglot.frontend.Source;
import polyglot.lex.Lexer;
import polyglot.lex.Operator;
import polyglot.lex.Token;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;

public class JL8TokenCombiningLexer extends Lexer_c implements Lexer {
    private Token bufferedToken = null;

    public JL8TokenCombiningLexer(java.io.Reader reader, Source file, ErrorQueue eq) {
        super(reader, file, eq);
    }

    @Override
    public Token nextToken() throws IOException {
        Token token = this.nextTokenUsingBuffer();
        if (token.symbol() != sym.RPAREN) return token;
        Token nextToken = this.nextTokenUsingBuffer();
        if (nextToken.symbol() == sym.ARROW) {
            Position positionUnion = new Position(token.getPosition(),nextToken.getPosition());
            return new Operator(positionUnion, ") ->", sym.RPAREN_ARROW);
        } else {
            this.bufferedToken = nextToken;
            return token;
        }
    }

    private Token nextTokenUsingBuffer() throws IOException {
        Token buffered = this.bufferedToken;
        if (buffered != null) {
            this.bufferedToken = null;
            return buffered;
        }
        return super.nextToken();
    }
}
