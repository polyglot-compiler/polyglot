/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.ast;

import polyglot.ast.JL;
import polyglot.ast.AbstractDelFactory_c;
import pao.extension.PaoInstanceofDel_c;

/**
 * Delegate factory for the pao extension. The delegate factory 
 * is responsible for creating <code>JL</code> delegate objects, and is
 * used only by the <code>NodeFactory</code>. 
 */
public class PaoDelFactory_c extends AbstractDelFactory_c {

	/**
	 * @see AbstractDelFactory_c#delInstanceofImpl()
	 */
	protected JL delInstanceofImpl() {
		return new PaoInstanceofDel_c();
	}
}
