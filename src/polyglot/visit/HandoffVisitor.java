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

package polyglot.visit;

import polyglot.ast.Node;
import polyglot.ast.SourceCollection;
import polyglot.ast.SourceFile;
import polyglot.frontend.ExtensionInfo;

/**
 * This visitor adds jobs for <code>SourceFile</code>s in the AST to the
 * schedule of another extension.
 */
public class HandoffVisitor extends NodeVisitor {
    protected ExtensionInfo ext;

    public HandoffVisitor(ExtensionInfo ext) {
        this.ext = ext;
    }

    @Override
    public Node override(Node n) {
        if (n instanceof SourceFile || n instanceof SourceCollection) {
            return null;
        }
        return n;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof SourceFile) {
            SourceFile sf = (SourceFile) n;
            ext.scheduler().addJob(sf.source(), sf);
        }
        return n;
    }
}
