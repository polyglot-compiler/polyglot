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
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;

/**
 * TypedTranslator extends Translator for type-directed code generation.
 * The base Translator uses types only to generate more readable code.
 * If an ambiguous or untyped AST node is encountered, code generation
 * continues. In contrast, with TypedTranslator, encountering an
 * ambiguous or untyped node is considered internal compiler error.
 * TypedTranslator should be used when the output AST is expected to be
 * (or required to be) type-checked before code generation.
 */
public class TypedTranslator extends Translator {

    public TypedTranslator(Job job, TypeSystem ts, NodeFactory nf,
            TargetFactory tf) {
        super(job, ts, nf, tf);
    }

    @Override
    public void print(Node parent, Node child, CodeWriter w) {
        if (context == null) {
            throw new InternalCompilerError("Null context found during type-directed code generation.",
                                            child.position());
        }

        if (parent != null
                && (!parent.isDisambiguated() || !parent.isTypeChecked())) {
            throw new InternalCompilerError("Untyped AST node found during type-directed code generation.",
                                            parent.position());
        }

        if (child != null
                && (!child.isDisambiguated() || !child.isTypeChecked())) {
            throw new InternalCompilerError("Untyped AST node found during type-directed code generation.",
                                            child.position());
        }

        super.print(parent, child, w); // XXX This won't work -- child is null (Findbugs)
    }
}
