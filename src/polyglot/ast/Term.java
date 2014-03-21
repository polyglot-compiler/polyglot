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

import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.SubtypeSet;
import polyglot.visit.CFGBuilder;

/**
 * A {@code Term} represents any Java expression or statement on which
 * dataflow can be performed.
 */
public interface Term extends Node {
    /**
     * Indicates to dataflow methods that we are looking at the entry of a term.
     */
    public static final int ENTRY = 1;

    /**
     * Indicates to dataflow methods that we are looking at the exit of a term.
     */
    public static final int EXIT = 0;

    /**
     * Returns true if the term is reachable.  This attribute is not
     * guaranteed correct until after the reachability pass.
     *
     * @see polyglot.visit.ReachChecker
     */
    boolean reachable();

    /**
     * Set the reachability of this term.
     */
    Term reachable(boolean reachable);

    /**
     * List of Types with all exceptions possibly thrown by this term.
     * The list is not necessarily correct until after exception-checking.
     * {@code polyglot.ast.NodeOps.throwTypes()} is similar, but exceptions
     * are not propagated to the containing node.
     */
    SubtypeSet exceptions();

    Term exceptions(SubtypeSet exceptions);

    class Instance extends Term_c {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public Instance(Position pos, Ext ext) {
            super(pos, ext);
            assert (ext != null);
        }

        @Override
        public final Node extRewrite(ExtensionRewriter rw)
                throws SemanticException {
            throw new InternalCompilerError("This term cannot be represented in the "
                    + "target language and should have been rewritten: " + this);
        }

        @Override
        public final Term firstChild() {
            throw new InternalCompilerError("Unexpected invocation from extension object.");
        }

        @Override
        public final <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
            throw new InternalCompilerError("Unexpected invocation from extension object.");
        }
    }
}
