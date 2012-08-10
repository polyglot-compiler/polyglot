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

import java.util.List;

import polyglot.types.Context;
import polyglot.util.Position;

/**
 * A <code>SwitchBlock</code> is a list of statements within a switch.
 */
public class SwitchBlock_c extends AbstractBlock_c implements SwitchBlock {
    public SwitchBlock_c(Position pos, List<Stmt> statements) {
        super(pos, statements);
    }

    /**
     * A <code>SwitchBlock</code> differs from a normal block in that 
     * declarations made in the context of the switch block are in the scope 
     * following the switch block. For example
     * <pre>
     * switch (i) { 
     *     case 0: 
     *       int i = 4; 
     *     case 1: 
     *       // i is in scope, but may not have been initialized.
     *     ...
     * } 
     * </pre>
     */
    @Override
    public Context enterScope(Context c) {
        return c;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.SwitchBlock(this.position, statements);
    }

}
