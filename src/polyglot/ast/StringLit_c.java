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

package polyglot.ast;

import java.util.LinkedList;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.StringUtil;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code StringLit} represents an immutable instance of a
 * {@code String} which corresponds to a literal string in Java code.
 */
public class StringLit_c extends Lit_c implements StringLit {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected String value;

//    @Deprecated
    public StringLit_c(Position pos, String value) {
        this(pos, value, null);
    }

    public StringLit_c(Position pos, String value, Ext ext) {
        super(pos, ext);
        assert value != null;
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public StringLit value(String value) {
        return value(this, value);
    }

    protected <N extends StringLit_c> N value(N n, String value) {
        if (n.value == value) return n;
        n = copyIfNeeded(n);
        n.value = value;
        return n;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().String());
    }

    @Override
    public String toString() {
        if (StringUtil.unicodeEscape(value).length() > 11) {
            return "\"" + StringUtil.unicodeEscape(value).substring(0, 8)
                    + "...\"";
        }

        return "\"" + StringUtil.unicodeEscape(value) + "\"";
    }

    protected int MAX_LENGTH = 60;

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write("\"");
        w.write(StringUtil.escape(value));
        w.write("\"");
        w.end();
    }

    /**
     * Break a long string literal into a concatenation of small string
     * literals.  This avoids messing up the pretty printer and editors.
     */
    protected List<String> breakupString() {
        List<String> result = new LinkedList<>();
        int n = value.length();
        int i = 0;

        while (i < n) {
            int j;

            // Compensate for the unicode transformation by computing
            // the length of the encoded string.
            int len = 0;

            for (j = i; j < n; j++) {
                char c = value.charAt(j);
                int k = StringUtil.unicodeEscape(c).length();
                if (len + k > MAX_LENGTH) break;
                len += k;
            }

            result.add(value.substring(i, j));

            i = j;
        }

        if (result.isEmpty()) {
            // This should only happen when value == "".
            if (!value.equals("")) {
                throw new InternalCompilerError("breakupString failed");
            }
            result.add(value);
        }

        return result;
    }

    @Override
    public Object constantValue(Lang lang) {
        return value;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.StringLit(position, value);
    }

}
