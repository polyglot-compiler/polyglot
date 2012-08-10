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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Formal;
import polyglot.ast.Import;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.frontend.Compiler;
import polyglot.util.CodeWriter;

/**
 * The <code>NodeScrambler</code> is test case generator of sorts. Since it
 * is ofter useful to introduce ``random'' errors into source code, this
 * class provides a way of doing so in a semi-structed manner. The process
 * takes place in two phases. First, a "FirstPass" is made to collect 
 * a list of nodes and their parents. Then a second pass is made to randomly 
 * replace a branch of the tree with another suitable branch. 
 */
public class NodeScrambler extends NodeVisitor {
    public FirstPass fp;

    protected HashMap<Node, LinkedList<Node>> pairs;
    protected LinkedList<Node> nodes;
    protected LinkedList<Node> currentParents;
    protected long seed;
    protected Random ran;
    protected boolean scrambled = false;
    protected CodeWriter cw;

    public NodeScrambler() {
        this(new Random().nextLong());
    }

    /**
     * Create a new <code>NodeScrambler</code> with the given random number
     * generator seed.
     */
    public NodeScrambler(long seed) {
        this.fp = new FirstPass();

        this.pairs = new HashMap<Node, LinkedList<Node>>();
        this.nodes = new LinkedList<Node>();
        this.currentParents = new LinkedList<Node>();
        this.cw = Compiler.createCodeWriter(System.err, 72);
        this.seed = seed;

        this.ran = new Random(seed);
    }

    /**
     * Scans throught the AST, create a list of all nodes present, along with
     * the set of parents for each node in the tree. <b>This visitor should be
     * run before the main <code>NodeScrambler</code> visits the tree.</b>
     */
    public class FirstPass extends NodeVisitor {
        @Override
        public NodeVisitor enter(Node n) {
            @SuppressWarnings("unchecked")
            LinkedList<Node> clone = (LinkedList<Node>) currentParents.clone();
            pairs.put(n, clone);
            nodes.add(n);

            currentParents.add(n);
            return this;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            currentParents.remove(n);
            return n;
        }
    }

    public long getSeed() {
        return seed;
    }

    @Override
    public Node override(Node n) {
        if (coinFlip()) {
            Node m = potentialScramble(n);
            if (m == null) {
                /* No potential replacement. */
                return null;
            }
            else {
                scrambled = true;

                try {
                    System.err.println("Replacing:");
                    n.del().dump(System.err);
                    System.err.println("With:");
                    m.del().dump(System.err);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return m;
            }
        }
        else {
            return null;
        }
    }

    protected boolean coinFlip() {
        if (scrambled) {
            return false;
        }
        else {
            if (ran.nextDouble() > 0.9) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    protected Node potentialScramble(Node n) {
        Class<? extends Node> required = Node.class;

        if (n instanceof SourceFile) {
            return null;
        }
        if (n instanceof Import) {
            required = Import.class;
        }
        else if (n instanceof TypeNode) {
            required = TypeNode.class;
        }
        else if (n instanceof ClassDecl) {
            required = ClassDecl.class;
        }
        else if (n instanceof ClassMember) {
            required = ClassMember.class;
        }
        else if (n instanceof Formal) {
            required = Formal.class;
        }
        else if (n instanceof Expr) {
            required = Expr.class;
        }
        else if (n instanceof Block) {
            required = Block.class;
        }
        else if (n instanceof Catch) {
            required = Catch.class;
        }
        else if (n instanceof LocalDecl) {
            required = LocalDecl.class;
        }
        else if (n instanceof Stmt) {
            required = Stmt.class;
        }

        LinkedList<Node> parents = pairs.get(n);
        boolean isParent;

        for (Node m : nodes) {
            if (required.isAssignableFrom(m.getClass())) {

                isParent = false;
                for (Node m2 : parents) {
                    if (m == m2) {
                        isParent = true;
                    }
                }

                if (!isParent && m != n) {
                    return m;
                }
            }
        }

        return null;
    }
}
