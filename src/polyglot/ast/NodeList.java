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
 * A <code>NodeList</code> represents a list of AST nodes. <code>NodeList</code>
 * s are not intended to appear as part of the AST. When a node is visited, it
 * may replace itself with multiple nodes by returning a <code>NodeList</code>
 * to the visitor. The rewritten node's parent would then be responsible for
 * properly splicing those nodes into the AST.
 */
public interface NodeList extends Node {
	/**
	 * Get the <code>NodeFactory</code> to use when converting the list to a
	 * proper AST node.
	 */
	NodeFactory nodeFactory();

	/** Get the nodes contained in the list. */
	List nodes();

	/** Set the nodes contained in the list. */
	NodeList nodes(List nodes);

	/** Convert the list into a <code>Block</code>. */
	Block toBlock();
}
