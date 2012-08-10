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

/**
 * A <code>Switch</code> is an immutable representation of a Java
 * <code>switch</code> statement.  Such a statement has an expression which
 * is evaluated to determine where to branch to, an a list of labels
 * and block statements which are conditionally evaluated.  One of the
 * labels, rather than having a constant expression, may be lablled
 * default.
 */
public interface Switch extends CompoundStmt {
    /** The expression on which to switch. */
    Expr expr();

    /** Set the expression on which to switch. */
    Switch expr(Expr expr);

    /** List of switch elements: case statements or blocks.
     * @return A list of {@link polyglot.ast.SwitchElement SwitchElement}.
     */
    List<SwitchElement> elements();

    /** Set the list of switch elements: case statements or blocks.
     * @param elements A list of {@link polyglot.ast.SwitchElement SwitchElement}.
     */
    Switch elements(List<SwitchElement> elements);
}
