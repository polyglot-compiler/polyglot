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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SubtypeSet;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * An immutable representation of a <code>try</code> block, one or more
 * <code>catch</code> blocks, and an optional <code>finally</code> block.
 */
public class Try_c extends Stmt_c implements Try {
    protected Block tryBlock;
    protected List<Catch> catchBlocks;
    protected Block finallyBlock;

    public Try_c(Position pos, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock) {
        super(pos);
        assert (tryBlock != null && catchBlocks != null); // finallyBlock may be null, catchBlocks empty
        assert (!catchBlocks.isEmpty() || finallyBlock != null); // must be either try-catch or try(-catch)-finally
        this.tryBlock = tryBlock;
        this.catchBlocks = ListUtil.copy(catchBlocks, true);
        this.finallyBlock = finallyBlock;
    }

    /** Get the try block of the statement. */
    @Override
    public Block tryBlock() {
        return this.tryBlock;
    }

    /** Set the try block of the statement. */
    @Override
    public Try tryBlock(Block tryBlock) {
        Try_c n = (Try_c) copy();
        n.tryBlock = tryBlock;
        return n;
    }

    /** Get the catch blocks of the statement. */
    @Override
    public List<Catch> catchBlocks() {
        return Collections.unmodifiableList(this.catchBlocks);
    }

    /** Set the catch blocks of the statement. */
    @Override
    public Try catchBlocks(List<Catch> catchBlocks) {
        Try_c n = (Try_c) copy();
        n.catchBlocks = ListUtil.copy(catchBlocks, true);
        return n;
    }

    /** Get the finally block of the statement. */
    @Override
    public Block finallyBlock() {
        return this.finallyBlock;
    }

    /** Set the finally block of the statement. */
    @Override
    public Try finallyBlock(Block finallyBlock) {
        Try_c n = (Try_c) copy();
        n.finallyBlock = finallyBlock;
        return n;
    }

    /** Reconstruct the statement. */
    protected Try_c reconstruct(Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock) {
        if (tryBlock != this.tryBlock
                || !CollectionUtil.equals(catchBlocks, this.catchBlocks)
                || finallyBlock != this.finallyBlock) {
            Try_c n = (Try_c) copy();
            n.tryBlock = tryBlock;
            n.catchBlocks = ListUtil.copy(catchBlocks, true);
            n.finallyBlock = finallyBlock;
            return n;
        }

        return this;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Block tryBlock = (Block) visitChild(this.tryBlock, v);
        List<Catch> catchBlocks = visitList(this.catchBlocks, v);
        Block finallyBlock = (Block) visitChild(this.finallyBlock, v);
        return reconstruct(tryBlock, catchBlocks, finallyBlock);
    }

    /**
     * Bypass all children when peforming an exception check.
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
     * via the exceptionChecker's override method (i.e, doesn't follow the
     * standard model for visitation.  
     *
     * @param ec The ExceptionChecker that was run against the 
     * child node. It contains the exceptions that can be thrown by the try
     * block.
     */
    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        TypeSystem ts = ec.typeSystem();
        ExceptionChecker origEC = ec;

        if (this.finallyBlock != null && !this.finallyBlock.reachable()) {
            // the finally block cannot terminate normally.
            // This implies that exceptions thrown in the try and catch
            // blocks will not propogate upwards.
            // Prevent exceptions from propagation upwards past the finally
            // block. (The original exception checker will be used
            // for checking the finally block).
            ec = ec.pushCatchAllThrowable();
        }

        ExceptionChecker newec = ec.push();
        for (ListIterator<Catch> i =
                this.catchBlocks.listIterator(this.catchBlocks.size()); i.hasPrevious();) {
            Catch cb = i.previous();
            Type catchType = cb.catchType();

            newec = newec.push(catchType);
        }

        // Visit the try block.
        Block tryBlock = (Block) this.visitChild(this.tryBlock, newec);

        SubtypeSet caught = new SubtypeSet(ts.Throwable());

        // Walk through our catch blocks, making sure that they each can 
        // catch something.
        for (Catch cb : this.catchBlocks) {
            Type catchType = cb.catchType();

            // Check if the exception has already been caught.
            if (caught.contains(catchType)) {
                throw new SemanticException("The exception \""
                                                    + catchType
                                                    + "\" has been caught by an earlier catch block.",
                                            cb.position());
            }

            caught.add(catchType);
        }

        // now visit the catch blocks, using the original exception checker
        List<Catch> catchBlocks = new ArrayList<Catch>(this.catchBlocks.size());

        for (Catch cb : this.catchBlocks) {
            ec = ec.push();
            cb = (Catch) this.visitChild(cb, ec);
            catchBlocks.add(cb);
            ec = ec.pop();
        }

        Block finallyBlock = null;

        if (this.finallyBlock != null) {
            ec = origEC;

            finallyBlock = (Block) this.visitChild(this.finallyBlock, ec);

            if (!this.finallyBlock.reachable()) {
                // warn the user
//              ###Don't warn, some versions of javac don't.              
//              ec.errorQueue().enqueue(ErrorInfo.WARNING,
//              "The finally block cannot complete normally", 
//              finallyBlock.position());
            }

            ec = ec.pop();
        }
        // now that all the exceptions have been added to the exception checker,
        // call the super method, which should set the exceptions field of 
        // Term_c.
        Try_c t = (Try_c) super.exceptionCheck(ec);

        return t.reconstruct(tryBlock, catchBlocks, finallyBlock);
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
        w.write("try");
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
        return nf.Try(this.position,
                      this.tryBlock,
                      this.catchBlocks,
                      this.finallyBlock);
    }

}
