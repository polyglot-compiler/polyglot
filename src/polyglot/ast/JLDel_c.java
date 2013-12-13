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

import java.io.Serializable;

import polyglot.util.SerialVersionUID;

/**
 * <code>JL_c</code> is the super class of JL node delegate objects.
 * It defines default implementations of the methods which implement compiler
 * passes, dispatching to the node to perform the actual work of the pass.
 * Language extensions may subclass <code>JL_c</code> for individual node
 * classes or may reimplement all compiler passes in a new class implementing
 * the <code>JL</code> interface.
 */
public class JLDel_c implements JLDel, Serializable {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static final JLDel instance = new JLDel_c();

//    Node node;
//
//    /** Create an uninitialized delegate. It must be initialized using the init() method.
//     */
    protected JLDel_c() {
    }

//    /** The <code>JL</code> object we dispatch to, by default, the node
//     * itself, but possibly another delegate.
//     */
//    public JLDel jl() {
//        return node();
//    }
//
    @Override
    public NodeOps NodeOps(Node n) {
        return n;
    }

    @Override
    public CallOps CallOps(Node n) {
        return (CallOps) n;
    }

    @Override
    public ClassDeclOps ClassDeclOps(Node n) {
        return (ClassDeclOps) n;
    }

    @Override
    public NewOps NewOps(Node n) {
        return (NewOps) n;
    }

    @Override
    public ProcedureDeclOps ProcedureDeclOps(Node n) {
        return (ProcedureDeclOps) n;
    }

    @Override
    public TryOps TryOps(Node n) {
        return (TryOps) n;
    }

//    @Override
//    public void init(Node n) {
//        assert (this.node == null);
//        this.node = n;
//    }
//
//    @Override
//    public Node node() {
//        return this.node;
//    }
//
//    @Override
//    public Object copy() {
//        try {
//            JLDel_c copy = (JLDel_c) super.clone();
//            copy.node = null; // uninitialize
//            return copy;
//        }
//        catch (CloneNotSupportedException e) {
//            throw new InternalCompilerError("Unable to clone a delegate");
//        }
//    }
}
