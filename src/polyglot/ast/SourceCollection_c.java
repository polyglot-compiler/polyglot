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

import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A <code>SourceCollection</code> represents a collection of source files.
 */
public class SourceCollection_c extends Node_c implements SourceCollection {
    protected List<SourceFile> sources;

    public SourceCollection_c(Position pos, List<SourceFile> sources) {
        super(pos);
        assert (sources != null);
        this.sources = ListUtil.copy(sources, true);
    }

    @Override
    public String toString() {
        return sources.toString();
    }

    /** Get the source files. */
    @Override
    public List<SourceFile> sources() {
        return this.sources;
    }

    /** Set the statements of the block. */
    @Override
    public SourceCollection sources(List<SourceFile> sources) {
        SourceCollection_c n = (SourceCollection_c) copy();
        n.sources = ListUtil.copy(sources, true);
        return n;
    }

    /** Reconstruct the collection. */
    protected SourceCollection_c reconstruct(List<SourceFile> sources) {
        if (!CollectionUtil.equals(sources, this.sources)) {
            SourceCollection_c n = (SourceCollection_c) copy();
            n.sources = ListUtil.copy(sources, true);
            return n;
        }

        return this;
    }

    /** Visit the children of the block. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        List<SourceFile> sources = visitList(this.sources, v);
        return reconstruct(sources);
    }

    /** Write the source files to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        for (SourceFile s : sources) {
            print(s, w, tr);
            w.newline(0);
        }
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.SourceCollection(this.position, this.sources);
    }

}
