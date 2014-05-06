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

package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Try_c;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * An immutable representation of a {@code try}-with-resources block.
 */
public class TryWithResources_c extends Try_c implements TryWithResources {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<LocalDecl> resources;

    public TryWithResources_c(Position pos, List<LocalDecl> resources,
            Block tryBlock, List<Catch> catchBlocks, Block finallyBlock) {
        super(pos, tryBlock, catchBlocks, finallyBlock);
        assert_(pos, tryBlock, catchBlocks, finallyBlock);
        this.resources = ListUtil.copy(resources, true);
    }

    @Override
    protected void assert_(Position pos, Block tryBlock,
            List<Catch> catchBlocks, Block finallyBlock) {
        assert (tryBlock != null); // catchBlock and finallyBlock may be null
    }

    @Override
    public List<LocalDecl> resources() {
        return this.resources;
    }

    @Override
    public TryWithResources resources(List<LocalDecl> resources) {
        return resources(this, resources);
    }

    protected <N extends TryWithResources_c> N resources(N n,
            List<LocalDecl> resources) {
        if (CollectionUtil.equals(n.resources, resources)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.resources = ListUtil.copy(resources, true);
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends TryWithResources_c> N reconstruct(N n,
            List<LocalDecl> resources, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock) {
        n = super.reconstruct(n, tryBlock, catchBlocks, finallyBlock);
        n = resources(n, resources);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<LocalDecl> resources = visitList(this.resources, v);
        Block tryBlock = visitChild(this.tryBlock, v);
        List<Catch> catchBlocks = visitList(this.catchBlocks, v);
        Block finallyBlock = visitChild(this.finallyBlock, v);
        return reconstruct(this, resources, tryBlock, catchBlocks, finallyBlock);
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        TryWithResources_c n = (TryWithResources_c) super.exceptionCheck(ec);

        ExceptionChecker ecTryBlockEntry = ec;
        if (this.finallyBlock != null && !this.finallyBlock.reachable()) {
            // the finally block cannot terminate normally.
            // This implies that exceptions thrown in the try and catch
            // blocks will not propagate upwards.
            // Prevent exceptions from propagation upwards past the finally
            // block. (The original exception checker will be used
            // for checking the finally block).
            ecTryBlockEntry = ecTryBlockEntry.pushCatchAllThrowable();
        }

        ExceptionChecker ecTryBlock =
                ec.lang().constructTryBlockExceptionChecker(n, ecTryBlockEntry);

        // Visit the resources.
        List<LocalDecl> resources = n.visitList(n.resources, ecTryBlock);
        n = resources(n, resources);
        return n;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("try (");
        int count = 0;
        for (LocalDecl l : resources) {
            if (count++ > 2) {
                sb.append("...");
                break;
            }

            sb.append(l);
            sb.append(" ");
        }
        sb.append(") ");
        sb.append(tryBlock.toString());

        count = 0;
        for (Catch cb : catchBlocks) {
            if (count++ > 2) {
                sb.append("...");
                break;
            }

            sb.append(" ");
            sb.append(cb.toString());
        }

        if (finallyBlock != null) {
            sb.append(" finally ");
            sb.append(finallyBlock.toString());
        }

        return sb.toString();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("try (");
        w.begin(0);

        int count = 0;
        for (LocalDecl l : resources) {
            if (count++ > 0) w.newline(0);
            print(l, w, tr);
        }
        w.end();
        w.write(")");
        printSubStmt(tryBlock, w, tr);

        for (Catch cb : catchBlocks) {
            w.newline(0);
            printBlock(cb, w, tr);
        }

        if (finallyBlock != null) {
            w.newline(0);
            w.write("finally");
            printSubStmt(finallyBlock, w, tr);
        }
    }

    @Override
    public Term firstChild() {
        return listChild(resources, null);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.push(this).visitCFGList(resources, tryBlock, ENTRY);
        return super.acceptCFG(v, succs);
    }
}
