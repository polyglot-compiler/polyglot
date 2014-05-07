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
package polyglot.ext.jl5.visit;

import java.util.List;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.JL5Import;
import polyglot.ext.jl5.types.JL5ImportTable;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.StringUtil;
import polyglot.visit.InitImportsVisitor;
import polyglot.visit.NodeVisitor;

/** Visitor which traverses the AST constructing type objects. */
@Deprecated
public class JL5InitImportsVisitor extends InitImportsVisitor {
    public JL5InitImportsVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        Node r = super.leaveCall(old, n, v);
        if (r instanceof Import) {
            Import im = (Import) n;
            JL5ImportTable it = (JL5ImportTable) this.importTable;

            if (im.kind() == JL5Import.SINGLE_STATIC_MEMBER) {
                String id = StringUtil.getShortNameComponent(im.name());
                checkForConflicts(id, it.singleTypeImports(), im.position());
                it.addSingleStaticImport(im.name(), im.position());
            }
            else if (im.kind() == JL5Import.STATIC_ON_DEMAND) {
                it.addStaticOnDemandImport(im.name(), im.position());
            }
            else if (im.kind() == Import.SINGLE_TYPE) {
                // just check for conflicts
                String id = StringUtil.getShortNameComponent(im.name());
                checkForConflicts(id, it.singleStaticImports(), im.position());
            }
        }

        return r;
    }

    private void checkForConflicts(String id, List<String> imports,
            Position position) throws SemanticException {
        for (String other : imports) {
            String name = StringUtil.getShortNameComponent(other);
            if (id.equals(name)) {
                throw new SemanticException("The import statement "
                                                    + this
                                                    + " collides with import statement "
                                                    + other + " .",
                                            position);
            }
        }

    }
}
