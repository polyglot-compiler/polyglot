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

package polyglot.frontend;

import polyglot.ast.Node;
import polyglot.frontend.goals.Goal;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

/** An output pass generates output code from the processed AST. */
public class PrettyPrintPass extends AbstractPass {
    protected PrettyPrinter pp;
    protected CodeWriter w;

    /**
     * Create a PrettyPrinter.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public PrettyPrintPass(Goal goal, CodeWriter w, PrettyPrinter pp) {
        super(goal);
        this.pp = pp;
        this.w = w;
    }

    @Override
    public boolean run() {
        Node ast = goal.job().ast();

        if (ast == null) {
            w.write("<<<< null AST >>>>");
        }
        else {
            pp.printAst(ast, w);
        }

        return true;
    }
}
