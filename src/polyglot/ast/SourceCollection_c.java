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

import polyglot.ast.*;
import polyglot.util.*;
import polyglot.types.*;
import polyglot.visit.*;
import java.util.*;

/**
 * A <code>SourceCollection</code> represents a collection of source files.
 */
public class SourceCollection_c extends Node_c implements SourceCollection
{
    protected List sources;

    public SourceCollection_c(Position pos, List sources) {
	super(pos);
	assert(sources != null);
	this.sources = TypedList.copyAndCheck(sources, SourceFile.class, true);
    }

    public String toString() {
	return sources.toString();
    }

    /** Get the source files. */
    public List sources() {
	return this.sources;
    }

    /** Set the statements of the block. */
    public SourceCollection sources(List sources) {
	SourceCollection_c n = (SourceCollection_c) copy();
	n.sources = TypedList.copyAndCheck(sources, SourceFile.class, true);
	return n;
    }

    /** Reconstruct the collection. */
    protected SourceCollection_c reconstruct(List sources) {
	if (! CollectionUtil.equals(sources, this.sources)) {
	    SourceCollection_c n = (SourceCollection_c) copy();
	    n.sources = TypedList.copyAndCheck(sources, SourceFile.class, true);
	    return n;
	}

	return this;
    }

    /** Visit the children of the block. */
    public Node visitChildren(NodeVisitor v) {
        List sources = visitList(this.sources, v);
	return reconstruct(sources);
    }

    /** Write the source files to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        for (Iterator i = sources.iterator(); i.hasNext(); ) {
            SourceFile s = (SourceFile) i.next();
            print(s, w, tr);
            w.newline(0);
        }
    }
    
    public Node copy(NodeFactory nf) {
        return nf.SourceCollection(this.position, this.sources);
    }

}
