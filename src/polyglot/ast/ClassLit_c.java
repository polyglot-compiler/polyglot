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

import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>ClassLit</code> represents a class literal expression. 
 * A class literal expressions is an expression consisting of the 
 * name of a class, interface, array, or primitive type followed by a period (.) 
 * and the token class. 
 */
public class ClassLit_c extends Lit_c implements ClassLit {
    protected TypeNode typeNode;

    public ClassLit_c(Position pos, TypeNode typeNode) {
        super(pos);
        assert (typeNode != null);
        this.typeNode = typeNode;
    }

    @Override
    public TypeNode typeNode() {
        return this.typeNode;
    }

    public ClassLit typeNode(TypeNode typeNode) {
        if (this.typeNode == typeNode) {
            return this;
        }
        ClassLit_c n = (ClassLit_c) copy();
        n.typeNode = typeNode;
        return n;
    }

    /**
     * Cannot return the correct object (except for maybe
     * some of the primitive arrays), so we just return null here. 
     */
    public Object objValue() {
        return null;
    }

    @Override
    public Term firstChild() {
        return typeNode;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(typeNode, this, EXIT);
        return succs;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode tn = (TypeNode) visitChild(this.typeNode, v);
        return this.typeNode(tn);
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().Class());
    }

    @Override
    public String toString() {
        return typeNode.toString() + ".class";
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        print(typeNode, w, tr);
        w.write(".class");
        w.end();
    }

    /**
     * According to the JLS 2nd Ed, sec 15.28, a class literal 
     * is not a compile time constant.
     */
    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Object constantValue() {
        return null;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ClassLit(this.position, this.typeNode);
    }

}
