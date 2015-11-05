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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.SubtypeSet;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * An immutable representation of a {@code try} block, one or more
 * {@code catch} blocks, and an optional {@code finally} block.
 */
public class Try_c extends Stmt_c implements Try, TryOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Block tryBlock;
    protected List<Catch> catchBlocks;
    protected Block finallyBlock;

//    @Deprecated
    public Try_c(Position pos, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock) {
        this(pos, tryBlock, catchBlocks, finallyBlock, null);
    }

    public Try_c(Position pos, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock, Ext ext) {
        super(pos, ext);
        assert_(pos, tryBlock, catchBlocks, finallyBlock);
        this.tryBlock = tryBlock;
        this.catchBlocks = ListUtil.copy(catchBlocks, true);
        this.finallyBlock = finallyBlock;
    }

    protected void assert_(Position pos, Block tryBlock,
            List<Catch> catchBlocks, Block finallyBlock) {
        assert tryBlock != null && catchBlocks != null; // finallyBlock may be null, catchBlocks empty
        assert !catchBlocks.isEmpty() || finallyBlock != null; // must be either try-catch or try(-catch)-finally
    }

    @Override
    public Block tryBlock() {
        return tryBlock;
    }

    @Override
    public Try tryBlock(Block tryBlock) {
        return tryBlock(this, tryBlock);
    }

    protected <N extends Try_c> N tryBlock(N n, Block tryBlock) {
        if (n.tryBlock == tryBlock) return n;
        n = copyIfNeeded(n);
        n.tryBlock = tryBlock;
        return n;
    }

    @Override
    public List<Catch> catchBlocks() {
        return catchBlocks;
    }

    @Override
    public Try catchBlocks(List<Catch> catchBlocks) {
        return catchBlocks(this, catchBlocks);
    }

    protected <N extends Try_c> N catchBlocks(N n, List<Catch> catchBlocks) {
        if (CollectionUtil.equals(n.catchBlocks, catchBlocks)) return n;
        n = copyIfNeeded(n);
        n.catchBlocks = ListUtil.copy(catchBlocks, true);
        return n;
    }

    @Override
    public Block finallyBlock() {
        return finallyBlock;
    }

    @Override
    public Try finallyBlock(Block finallyBlock) {
        return finallyBlock(this, finallyBlock);
    }

    protected <N extends Try_c> N finallyBlock(N n, Block finallyBlock) {
        if (n.finallyBlock == finallyBlock) return n;
        n = copyIfNeeded(n);
        n.finallyBlock = finallyBlock;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Try_c> N reconstruct(N n, Block tryBlock,
            List<Catch> catchBlocks, Block finallyBlock) {
        n = tryBlock(n, tryBlock);
        n = catchBlocks(n, catchBlocks);
        n = finallyBlock(n, finallyBlock);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Block tryBlock = visitChild(this.tryBlock, v);
        List<Catch> catchBlocks = visitList(this.catchBlocks, v);
        Block finallyBlock = visitChild(this.finallyBlock, v);
        return reconstruct(this, tryBlock, catchBlocks, finallyBlock);
    }

    /**
     * Bypass all children when performing an exception check.
     * exceptionCheck(), called from ExceptionChecker.leave(),
     * will handle visiting children.
     */
    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        ec = (ExceptionChecker) super.exceptionCheckEnter(ec);
        return ec.bypassChildren(this);
    }

    /**
     * Performs exceptionChecking. This is a special method that is called
     * via the exceptionChecker's override method (i.e., doesn't follow the
     * standard model for visitation.
     *
     * @param ec The ExceptionChecker that was run against the
     * child node. It contains the exceptions that can be thrown by the try
     * block.
     */
    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        ExceptionChecker ecTryBlockEntry = ec;
        if (finallyBlock != null && !finallyBlock.reachable()) {
            // the finally block cannot terminate normally.
            // This implies that exceptions thrown in the try and catch
            // blocks will not propagate upwards.
            // Prevent exceptions from propagation upwards past the finally
            // block. (The original exception checker will be used
            // for checking the finally block).
            ecTryBlockEntry = ecTryBlockEntry.pushCatchAllThrowable();
        }

        ExceptionChecker ecTryBlock =
                ec.lang().constructTryBlockExceptionChecker(this,
                                                            ecTryBlockEntry);

        Try_c n = this;
        // Visit the try block.
        Block tryBlock = ec.lang().exceptionCheckTryBlock(n, ecTryBlock);
        n = tryBlock(n, tryBlock);

        List<Catch> catchBlocks =
                ec.lang().exceptionCheckCatchBlocks(n, ecTryBlockEntry);
        n = catchBlocks(n, catchBlocks);

        Block finallyBlock = ec.lang().exceptionCheckFinallyBlock(n, ec);
        n = finallyBlock(n, finallyBlock);

        for (Type exc : ec.lang().throwTypes(n, ec.typeSystem())) {
            ec.throwsException(exc, position());
        }
        n = exceptions(n, ec.throwsSet());

        return n;
    }

    @Override
    public Block exceptionCheckTryBlock(ExceptionChecker ec) {
        return this.visitChild(tryBlock,
                               ec.lang().constructTryBlockExceptionChecker(this,
                                                                           ec));
    }

    @Override
    public ExceptionChecker constructTryBlockExceptionChecker(
            ExceptionChecker ec) {

        ExceptionChecker newec = ec.push();

        // go through the catch blocks, from the end to the beginning, and push
        // an ExceptionChecker indicating that they catch exceptions of the appropriate
        // type.
        for (ListIterator<Catch> i =
                catchBlocks.listIterator(catchBlocks.size()); i.hasPrevious();) {
            Catch cb = i.previous();
            Type catchType = cb.catchType();

            newec = newec.push(catchType);
        }
        return newec;
    }

    @Override
    public List<Catch> exceptionCheckCatchBlocks(ExceptionChecker ec)
            throws SemanticException {
        // Walk through our catch blocks, making sure that they each can
        // catch something.
        SubtypeSet caught = new SubtypeSet(ec.typeSystem().Throwable());
        for (Catch cb : catchBlocks) {
            Type catchType = cb.catchType();

            // Check if the exception has already been caught.
            if (caught.contains(catchType)) {
                throw new SemanticException("The exception \"" + catchType
                        + "\" has been caught by an earlier catch block.",
                                            cb.position());
            }

            caught.add(catchType);
        }

        // now visit the catch blocks, using the original exception checker
        List<Catch> catchBlocks = new ArrayList<>(this.catchBlocks.size());

        for (Catch cb : this.catchBlocks) {
            cb = this.visitChild(cb, ec.push());
            catchBlocks.add(cb);
        }

        return catchBlocks;
    }

    @Override
    public Block exceptionCheckFinallyBlock(ExceptionChecker ec) {
        if (finallyBlock == null) {
            return null;
        }
        Block fb = this.visitChild(finallyBlock, ec.push());

        if (!finallyBlock.reachable()) {
            // warn the user
//              ###Don't warn, some versions of javac don't.
//              ec.errorQueue().enqueue(ErrorInfo.WARNING,
//              "The finally block cannot complete normally",
//              finallyBlock.position());
        }

        return fb;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("try ");
        sb.append(tryBlock.toString());

        int count = 0;

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
        w.begin(0);
        w.write("try");
        printSubStmt(tryBlock, w, tr);
        w.end();

        for (Catch cb : catchBlocks) {
            w.newline(0);
            printBlock(cb, w, tr);
        }

        if (finallyBlock != null) {
            w.newline(0);
            w.begin(0);
            w.write("finally");
            printSubStmt(finallyBlock, w, tr);
            w.end();
        }
    }

    @Override
    public Term firstChild() {
        return tryBlock;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        // Add edges from the try entry to any catch blocks for Error and
        // RuntimeException.
        TypeSystem ts = v.typeSystem();

        CFGBuilder<?> v1 = v.push(this, false);
        CFGBuilder<?> v2 = v.push(this, true);

        for (Type type : ts.uncheckedExceptions()) {
            v1.visitThrow(tryBlock, ENTRY, type);
        }

        // Handle the normal return case.  The throw case will be handled
        // specially.
        if (finallyBlock != null) {
            v1.visitCFG(tryBlock, finallyBlock, ENTRY);
            v.visitCFG(finallyBlock, this, EXIT);
        }
        else {
            v1.visitCFG(tryBlock, this, EXIT);
        }

        for (Catch cb : catchBlocks) {
            if (finallyBlock != null) {
                v2.visitCFG(cb, finallyBlock, ENTRY);
            }
            else {
                v2.visitCFG(cb, this, EXIT);
            }
        }

        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Try(position, tryBlock, catchBlocks, finallyBlock);
    }

}
