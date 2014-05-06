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

import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code ClassLit} represents a class literal expression. 
 * A class literal expressions is an expression consisting of the 
 * name of a class, interface, array, or primitive type followed by a period (.) 
 * and the token class. 
 */
public class ClassLit_c extends Lit_c implements ClassLit {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected TypeNode typeNode;

//    @Deprecated
    public ClassLit_c(Position pos, TypeNode typeNode) {
        this(pos, typeNode, null);
    }

    public ClassLit_c(Position pos, TypeNode typeNode, Ext ext) {
        super(pos, ext);
        assert (typeNode != null);
        this.typeNode = typeNode;
    }

    @Override
    public TypeNode typeNode() {
        return this.typeNode;
    }

    public ClassLit typeNode(TypeNode typeNode) {
        return typeNode(this, typeNode);
    }

    protected <N extends ClassLit_c> N typeNode(N n, TypeNode typeNode) {
        if (n.typeNode == typeNode) return n;
        n = copyIfNeeded(n);
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

    protected <N extends ClassLit_c> N reconstruct(N n, TypeNode typeNode) {
        n = typeNode(n, typeNode);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode tn = visitChild(this.typeNode, v);
        return reconstruct(this, tn);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().Class());
    }

    @Override
    public String toString() {
        return typeNode.toString() + ".class";
    }

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
    public boolean isConstant(Lang lang) {
        return false;
    }

    @Override
    public Object constantValue(Lang lang) {
        return null;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ClassLit(this.position, this.typeNode);
    }

}
