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

import java.util.List;

import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A {@code SourceCollection} represents a collection of source files.
 * This node should be used only during AST rewriting, just before code
 * generation in order to generate multiple target files from a single
 * AST.
 */
public class SourceCollection_c extends Node_c implements SourceCollection {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<SourceFile> sources;

//    @Deprecated
    public SourceCollection_c(Position pos, List<SourceFile> sources) {
        this(pos, sources, null);
    }

    public SourceCollection_c(Position pos, List<SourceFile> sources, Ext ext) {
        super(pos, ext);
        assert (sources != null);
        this.sources = ListUtil.copy(sources, true);
    }

    @Override
    public String toString() {
        return sources.toString();
    }

    @Override
    public List<SourceFile> sources() {
        return this.sources;
    }

    @Override
    public SourceCollection sources(List<SourceFile> sources) {
        return sources(this, sources);
    }

    protected <N extends SourceCollection_c> N sources(N n,
            List<SourceFile> sources) {
        if (CollectionUtil.equals(n.sources, sources)) return n;
        n = copyIfNeeded(n);
        n.sources = ListUtil.copy(sources, true);
        return n;
    }

    /** Reconstruct the collection. */
    protected <N extends SourceCollection_c> N reconstruct(N n,
            List<SourceFile> sources) {
        n = sources(n, sources);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<SourceFile> sources = visitList(this.sources, v);
        return reconstruct(this, sources);
    }

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
