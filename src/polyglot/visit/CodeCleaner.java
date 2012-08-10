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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.ast.Block;
import polyglot.ast.Branch;
import polyglot.ast.Labeled;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Return;
import polyglot.ast.Stmt;
import polyglot.ast.SwitchBlock;
import polyglot.ast.Throw;
import polyglot.util.Position;

/**
 * The <code>CodeCleaner</code> runs over the AST and performs some trivial
 * dead code elimination, while flattening blocks wherever possible.
 **/
public class CodeCleaner extends NodeVisitor {

    protected NodeFactory nf;
    protected AlphaRenamer alphaRen;

    /**
     * Creates a visitor for cleaning code.
     *
     * @param nf  The node factory to be used when generating new nodes.
     **/
    public CodeCleaner(NodeFactory nf) {
        this.nf = nf;
        this.alphaRen = new AlphaRenamer(nf);
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (!(n instanceof Block || n instanceof Labeled)) {
            return n;
        }

        // If we have a labeled block consisting of just one statement, then
        // flatten the block and label the statement instead.  We also flatten
        // labeled blocks when there is no reference to the label within the
        // block.
        if (n instanceof Labeled) {
            Labeled l = (Labeled) n;
            if (!(l.statement() instanceof Block)) {
                return n;
            }

            Block b = (Block) l.statement();
            if (b.statements().size() != 1) {
                if (labelRefs(b).contains(l.label())) {
                    return n;
                }

                // There's no reference to the label within the block, so flatten and
                // clean up dead code.
                return nf.Block(b.position(), clean(flattenBlock(b)));
            }

            // Alpha-rename local decls in the block that we're flattening.
            b = (Block) b.visit(alphaRen);
            return nf.Labeled(l.position(),
                              nf.Id(Position.compilerGenerated(), l.label()),
                              b.statements().get(0));
        }

        // Flatten any blocks that may be contained in this one, and clean up dead
        // code.
        Block b = (Block) n;
        List<Stmt> stmtList = clean(flattenBlock(b));

        if (b instanceof SwitchBlock) {
            return nf.SwitchBlock(b.position(), stmtList);
        }

        return nf.Block(b.position(), stmtList);
    }

    /**
     * Turns a Block into a list of Stmts.
     **/
    protected List<Stmt> flattenBlock(Block b) {
        List<Stmt> stmtList = new LinkedList<Stmt>();
        for (Stmt stmt : b.statements()) {
            if (stmt instanceof Block) {
                // Alpha-rename local decls in the block that we're flattening.
                stmt = (Stmt) stmt.visit(alphaRen);
                stmtList.addAll(((Block) stmt).statements());
            }
            else {
                stmtList.add(stmt);
            }
        }

        return stmtList;
    }

    /**
     * Performs some trivial dead code elimination on a list of statements.
     **/
    protected List<Stmt> clean(List<Stmt> l) {
        List<Stmt> stmtList = new LinkedList<Stmt>();
        for (Stmt stmt : l) {
            stmtList.add(stmt);

            if (stmt instanceof Branch || stmt instanceof Return
                    || stmt instanceof Throw) {
                return stmtList;
            }
        }

        return l;
    }

    /**
     * Traverses a Block and determines the set of label references.
     **/
    protected Set<String> labelRefs(Block b) {
        final Set<String> result = new HashSet<String>();
        b.visit(new NodeVisitor() {
            @Override
            public Node leave(Node old, Node n, NodeVisitor v) {
                if (n instanceof Branch) {
                    result.add(((Branch) n).label());
                }

                return n;
            }
        });

        return result;
    }
}
