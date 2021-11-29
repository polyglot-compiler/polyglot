/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 *
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.ext.jl8.parse;

import java.io.IOException;
import polyglot.frontend.Source;
import polyglot.lex.Operator;
import polyglot.lex.Token;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;

public class JL8TokenCombiningLexer extends Lexer_c {
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
            Position positionUnion = new Position(token.getPosition(), nextToken.getPosition());
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
