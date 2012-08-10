/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.util.CodeWriter;
import polyglot.util.Position;

/**
 * An integer literal: longs, ints, shorts, bytes, and chars.
 */
public abstract class NumLit_c extends Lit_c implements NumLit {
    protected long value;

    public NumLit_c(Position pos, long value) {
        super(pos);
        this.value = value;
    }

    /** Get the value of the expression. */
    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(value " + value + ")");
        w.end();
    }
}
