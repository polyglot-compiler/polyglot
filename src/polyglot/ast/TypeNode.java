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

import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;

/**
 * A {@code TypeNode} is the syntactic representation of a 
 * {@code Type} within the abstract syntax tree.
 */
public interface TypeNode extends Receiver, QualifierNode, Term {
    /** Set the type object for this node. */
    TypeNode type(Type type);

    /** Short name of the type, or null if not a {@code Named} type. */
    String name();

    class Instance extends TypeNode_c {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public Instance(Position pos, Ext ext) {
            super(pos, ext);
            assert (ext != null);
        }

        @Override
        public final Node extRewrite(ExtensionRewriter rw)
                throws SemanticException {
            throw new InternalCompilerError("This type node cannot be represented in the "
                    + "target language and should have been rewritten: " + this);
        }

        @Override
        public final void prettyPrint(CodeWriter w, PrettyPrinter tr) {
            throw new InternalCompilerError("Unexpected invocation from extension object:"
                    + this);
        }

    }
}
