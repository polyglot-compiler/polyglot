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

package polyglot.lex;

import polyglot.util.Position;

/** The base class of all tokens. */
public abstract class Token {
    protected Position position;
    protected int symbol;

    public Token(Position position, int symbol) {
        this.position = position;
        this.symbol = symbol;
    }

    public Position getPosition() {
        return position;
    }

    public int symbol() {
        return symbol;
    }

    protected static String escape(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++)
            switch (s.charAt(i)) {
            case '\t':
                sb.append("\\t");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            default:
                if (s.charAt(i) < 0x20
                        || (s.charAt(i) > 0x7e && s.charAt(i) < 0xFF))
                    sb.append("\\" + Integer.toOctalString(s.charAt(i)));
                else sb.append(s.charAt(i));
            }
        return sb.toString();
    }
}
