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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.JLang;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;

/** Visitor which finds shared AST nodes in a file. This can be used 
 * to find bugs in previous passes that violate non-sharing of AST nodes.
 * */
public class FindSharedASTVisitor extends NodeVisitor {

    private Map<Node, NodeStack> seenNodes = new HashMap<>();
    private NodeStack currentStack;

    public FindSharedASTVisitor(JLang lang) {
        super(lang);
    }

    @Override
    public NodeVisitor enter(Node n) {
        this.currentStack = new NodeStack(n, this.currentStack);
        if (seenNodes.containsKey(n)) {
            alreadySeenNode(n, seenNodes.get(n), this.currentStack);
        }
        else {
            seenNodes.put(n, currentStack);
        }
        return this;
    }

    /**
     * Called when a node is encountered that has been seen before.
     * @param n
     * @param stack1
     * @param stack2
     */
    protected void alreadySeenNode(Node n, NodeStack stack1, NodeStack stack2) {
        Node m = findCommonParent(stack1, stack2);
        if (m != null) {
            lang().prettyPrint(m, lang(), System.err);
        }

        throw new InternalCompilerError("Already seen node " + n + " ("
                + n.getClass().getSimpleName() + ") at " + stack1 + " and at "
                + stack2, n.position());
    }

    protected Node findCommonParent(NodeStack stack1, NodeStack stack2) {
        List<Node> list1 = new ArrayList<>();
        while (stack1 != null) {
            list1.add(stack1.n);
            stack1 = stack1.rest;
        }
        List<Node> list2 = new ArrayList<>();
        while (stack2 != null) {
            list2.add(stack2.n);
            stack2 = stack2.rest;
        }

        int i = list1.size();
        int j = list2.size();
        while (i > 0 && j > 0) {
            i--;
            j--;
            if (list1.get(i) != list2.get(j)) {
                return list1.get(i + 1);
            }
        }
        if (i > 0) {
            return list1.get(i - 1);
        }
        if (j > 0) {
            return list2.get(j - 1);
        }
        return null;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (this.currentStack != null) {
            this.currentStack = this.currentStack.rest;
        }
        return n;
    }

    public class NodeStack {
        final Node n;
        final NodeStack rest;

        NodeStack(Node n, NodeStack rest) {
            this.n = n;
            this.rest = rest;
        }

        NodeStack(Node n) {
            this.n = n;
            this.rest = null;
        }

        @Override
        public String toString() {
            if (rest == null) return "";
            return rest.toString() + "\n :: " + n + "(" + n.position() + "; "
                    + n.getClass().getSimpleName() + ")";
        }

    }

}
