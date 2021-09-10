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

package polyglot.visit;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.PackageNode;
import polyglot.ast.SourceFile;
import polyglot.frontend.Job;
import polyglot.types.ImportTable;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;

/**
 * Visitor which traverses the AST constructing type objects.
 * @deprecated Use TypeBuilder instead.
 */
@Deprecated
public class InitImportsVisitor extends ErrorHandlingVisitor {
    protected ImportTable importTable;

    public InitImportsVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public NodeVisitor enterCall(Node n) throws SemanticException {
        if (n instanceof SourceFile) {
            SourceFile sf = (SourceFile) n;

            PackageNode pn = sf.package_();

            ImportTable it;

            if (pn != null) {
                it = ts.importTable(sf.source().name(), pn.package_());
            } else {
                it = ts.importTable(sf.source().name(), null);
            }

            InitImportsVisitor v = (InitImportsVisitor) copy();
            v.importTable = it;
            return v;
        }

        return this;
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (n instanceof SourceFile) {
            SourceFile sf = (SourceFile) n;
            InitImportsVisitor v_ = (InitImportsVisitor) v;
            ImportTable it = v_.importTable;
            return sf.importTable(it);
        }
        if (n instanceof Import) {
            Import im = (Import) n;

            if (im.kind() == Import.SINGLE_TYPE) {
                this.importTable.addClassImport(im.name(), im.position());
            } else if (im.kind() == Import.TYPE_IMPORT_ON_DEMAND) {
                this.importTable.addTypeOnDemandImport(im.name(), im.position());
            }
        }

        return n;
    }
}
